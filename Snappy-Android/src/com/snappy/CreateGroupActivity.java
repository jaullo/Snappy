/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.snappy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.snappy.couchdb.Command;
import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.SpikaAsyncTask;
import com.snappy.couchdb.SpikaException;
import com.snappy.couchdb.SpikaForbiddenException;
import com.snappy.couchdb.model.Group;
import com.snappy.couchdb.model.GroupCategory;
import com.snappy.couchdb.model.GroupSearch;
import com.snappy.dialog.HookUpPasswordDialog;
import com.snappy.extendables.SpikaActivity;
import com.snappy.extendables.SpikaAsync;
import com.snappy.lazy.ImageLoader;
import com.snappy.utils.Const;
import com.snappy.utils.Logger;
import com.snappy.utils.Utils;

/**
 * CreateGroupActivity
 * 
 * Allows user to create new group on CouchDB.
 */

public class CreateGroupActivity extends SpikaActivity {

	private Button mBtnCreate;
	private Button mBtnBack;
	private EditText mEtGroupName;
	private EditText mEtGroupPassword;
	private EditText mEtGroupDescription;
	private ImageView mIvGroupImage;

	public static Bitmap gGroupImage = null;
	public static String gGroupImagePath = null;

	private final int GET_IMAGE_DIALOG = 8;
	private Dialog mGetImageDialog;

	private String mGroupName = "";
	private String mGroupPassword = "";
    private String mGroupAvatarId;
    private String mGroupAvatarThumbId;
	
	private Spinner mSpinnerCategory;
	private List<GroupCategory> mGroupCategories;
	
	private HookUpPasswordDialog mPasswordDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_group);

		initialization();
		onClickListeners();
	}

	private void initialization() {

		mPasswordDialog = new HookUpPasswordDialog(this, true);
		mBtnCreate = (Button) findViewById(R.id.btnCreateGroup);
		mBtnCreate.setTypeface(SpikaApp.getTfMyriadProBold());
		mEtGroupName = (EditText) findViewById(R.id.etCreateGroupName);
		mEtGroupName.setTypeface(SpikaApp.getTfMyriadPro());
		mEtGroupPassword = (EditText) findViewById(R.id.etCreateGroupPassword);
		mEtGroupPassword.setTypeface(SpikaApp.getTfMyriadPro());
		mEtGroupPassword.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mPasswordDialog.show();
				hideKeyboard();
				return false;
			}
		});
		mEtGroupDescription = (EditText) findViewById(R.id.etCreateGroupDescription);
		mEtGroupDescription.setTypeface(SpikaApp.getTfMyriadPro());
		mIvGroupImage = (ImageView) findViewById(R.id.ivGroupImage);
		mBtnBack = (Button) findViewById(R.id.btnBack);
		mBtnBack.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
		
		mSpinnerCategory = (Spinner) findViewById(R.id.spinnerCategory);
		new GetGroupCategoriesAsync(this).execute();
	}
	
	public void setNewPassword(String newPassword) {
		mGroupPassword = newPassword;
		if (mGroupPassword != null && !mGroupPassword.equals("")) {
			mEtGroupPassword.setText(R.string.YES);
		} else {
			mEtGroupPassword.setText(R.string.NO);
		}
		hideKeyboard();
	}

	@Override
	protected void setObjectsNull() {
		if (gGroupImage != null) {
			gGroupImage.recycle();
			gGroupImage = null;
		}
		unbindDrawables(findViewById(R.id.ivGroupImage));
		super.setObjectsNull();
	}

	private OnClickListener getImageClickListener() {

		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(GET_IMAGE_DIALOG);

			}
		};
	}

	private void onClickListeners() {

		mIvGroupImage.setOnClickListener(getImageClickListener());

		mBtnCreate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Group groupToCreate = new Group();
				mGroupName = mEtGroupName.getText().toString();
				groupToCreate.setName(mEtGroupName.getText().toString());
//				mGroupPassword = mEtGroupPassword.getText().toString();
//				groupToCreate
//						.setPassword(mEtGroupPassword.getText().toString());
				groupToCreate.setPassword(mGroupPassword);
				groupToCreate.setDescription(mEtGroupDescription.getText()
						.toString());
				GroupCategory selectedCategory = mGroupCategories.get(mSpinnerCategory.getSelectedItemPosition());
				groupToCreate.setCategoryId(selectedCategory.getId());
				groupToCreate.setCategoryName(selectedCategory.getTitle());

				if (nameAndPasswordAreValid(mGroupName, mGroupPassword) == true) {
//				    new AvailabilityAsync(CreateGroupActivity.this).execute(groupToCreate);
				    CouchDB.getGroupByNameAsync(groupToCreate.getName(), new CheckAvailabilityFinish(groupToCreate), CreateGroupActivity.this, true);
				}
			}
		});

		mBtnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CreateGroupActivity.this.finish();

			}
		});
	}

	private boolean nameAndPasswordAreValid(String name, String password) {
		String nameResult = Utils.checkName(this, name);
		String passwordResult = Utils.checkPasswordForGroup(this, password);
		
		if (!nameResult.equals(getString(R.string.name_ok))) {
			Toast.makeText(this, nameResult, Toast.LENGTH_SHORT).show();
			return false;
		} else if (!passwordResult.equals(getString(R.string.password_ok))){
			Toast.makeText(this, passwordResult, Toast.LENGTH_SHORT).show();
			return false;
		} else {
			return true;
		}
	}

//    private class AvailabilityAsync extends SpikaAsync<Group, Void, Group> {
//
//        private Group mGroup;
//        private Group mGroupFound;
//        private HookUpProgressDialog mProgressDialog;
//
//        protected AvailabilityAsync(Context context) {
//            super(context);
//            mProgressDialog = new HookUpProgressDialog(CreateGroupActivity.this);
//        }
//
//        
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            mProgressDialog.show();
//        }
//
//        @Override
//        protected Group doInBackground(Group... params) {
//            
//            mGroup = params[0];
//            mGroupFound = CouchDB.getGroupByName(mGroup.getName());
//            
//            return mGroupFound;
//            
//        }
//
//        @Override
//        protected void onPostExecute(Group result) {
//            super.onPostExecute(result);
//            mProgressDialog.dismiss();
//
//            if (mGroupFound != null) {
//                Toast.makeText(CreateGroupActivity.this, getString(R.string.groupname_taken), Toast.LENGTH_SHORT).show();
//            }  else {
//            	createGroupAsync(mGroup);
//            }
//        }
//    }
    
    private class CheckAvailabilityFinish implements ResultListener<Group> {

    	Group group;
    	
		public CheckAvailabilityFinish(Group group) {
			this.group = group;
		}

		@Override
		public void onResultsSucceded(Group result) {
			if (result != null) {
				Logger.debug("group", result.getName() + "");
                Toast.makeText(CreateGroupActivity.this, getString(R.string.groupname_taken), Toast.LENGTH_SHORT).show();
            }  else {
            	createGroupAsync(group);
            }
		}

		@Override
		public void onResultsFail() {			
		}
    }

    private void createGroupAsync (Group group) {
    	new SpikaAsyncTask<Void, Void, String>(new CreateGroup(group), new GroupCreatedFinish(), CreateGroupActivity.this, true).execute();
    }
    
    private class CreateGroup implements Command<String> {

    	Group group;
    	
    	public CreateGroup(Group group)
    	{
    		this.group = group;
    	}
    	
		@Override
		public String execute() throws JSONException, IOException,
				SpikaException, IllegalStateException, SpikaForbiddenException {
			if (gGroupImagePath != null) {

                String tmppath = CreateGroupActivity.this.getExternalCacheDir() + "/" + Const.TMP_BITMAP_FILENAME;              
                Bitmap originalBitmap = BitmapFactory.decodeFile(gGroupImagePath);
                
                Bitmap avatarBitmap = Utils.scaleBitmap(originalBitmap, Const.PICTURE_SIZE, Const.PICTURE_SIZE);
                Utils.saveBitmapToFile(avatarBitmap,tmppath);
                String avatarFileId = CouchDB.uploadFile(tmppath);
 
                Bitmap avatarThumb = Utils.scaleBitmap(originalBitmap, Const.AVATAR_THUMB_SIZE, Const.AVATAR_THUMB_SIZE);
                Utils.saveBitmapToFile(avatarThumb,tmppath);
                String avatarThumbFileId = CouchDB.uploadFile(tmppath);
        
                group.setAvatarFileId(avatarFileId);
                group.setAvatarThumbFileId(avatarThumbFileId);
                
			} else {
                group.setAvatarFileId(mGroupAvatarId);
                group.setAvatarThumbFileId(mGroupAvatarThumbId);
			}

			return CouchDB.createGroup(group);
		}
    }
    
    private class GroupCreatedFinish implements ResultListener<String> {

		@Override
		public void onResultsSucceded(String groupId) {
			if (groupId != null) {

				Toast.makeText(getApplicationContext(), R.string.create_group_ok,
						Toast.LENGTH_SHORT).show();

//				CouchDB.addFavoriteGroupAsync(groupId, new AddToFavoritesFinish(), CreateGroupActivity.this, true);

				finish();
				
			} else {
				Toast.makeText(getApplicationContext(),
						R.string.create_group_failed, Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onResultsFail() {
		}
    }
    
    private class AddToFavoritesFinish implements ResultListener<Boolean> {
		@Override
		public void onResultsSucceded(Boolean result) {
			finish();
		}
		@Override
		public void onResultsFail() {			
		}
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case GET_IMAGE_DIALOG:
			mGetImageDialog = new Dialog(CreateGroupActivity.this,
					R.style.TransparentDialogTheme);
			mGetImageDialog.getWindow().setGravity(Gravity.BOTTOM);
			mGetImageDialog.setContentView(R.layout.dialog_get_image);

			// Grab the window of the dialog, and change the width
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			Window window = mGetImageDialog.getWindow();
			lp.copyFrom(window.getAttributes());
			// This makes the dialog take up the full width
			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
			window.setAttributes(lp);

			final Button btnGallery = (Button) mGetImageDialog
					.findViewById(R.id.btnGallery);
			btnGallery.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
			btnGallery.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					Intent galleryIntent = new Intent(CreateGroupActivity.this,
							CameraCropActivity.class);
					galleryIntent.putExtra("type", "gallery");
					galleryIntent.putExtra("createGroup", true);
					CreateGroupActivity.this.startActivity(galleryIntent);
					mGetImageDialog.dismiss();

				}
			});

			final Button btnCamera = (Button) mGetImageDialog
					.findViewById(R.id.btnCamera);
			btnCamera.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
			btnCamera.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					Intent cameraIntent = new Intent(CreateGroupActivity.this,
							CameraCropActivity.class);
					cameraIntent.putExtra("type", "camera");
					cameraIntent.putExtra("createGroup", true);
					CreateGroupActivity.this.startActivity(cameraIntent);
					mGetImageDialog.dismiss();

				}
			});

			final Button btnRemovePhoto = (Button) mGetImageDialog
					.findViewById(R.id.btnRemovePhoto);
			btnRemovePhoto.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
			btnRemovePhoto.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					mGroupAvatarId = "";
					gGroupImage = null;
					Utils.displayImage(mGroupAvatarId, mIvGroupImage, ImageLoader.LARGE, R.drawable.group_stub_large, false);
					mGetImageDialog.dismiss();

				}
			});

			return mGetImageDialog;
		default:
			return null;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (gGroupImage != null) {
			mIvGroupImage.setImageBitmap(gGroupImage);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		Intent intent = new Intent(this, GroupsActivity.class);
		startActivity(intent);
	}

	private class GetGroupCategoriesAsync extends
			SpikaAsync<GroupSearch, Void, List<GroupCategory>> {

		protected GetGroupCategoriesAsync(Context context) {
			super(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected List<GroupCategory> backgroundWork(GroupSearch... params) throws ClientProtocolException, IOException, JSONException, SpikaException, IllegalStateException, SpikaForbiddenException {

			return CouchDB.findGroupCategories();
		}

		@Override
		protected void onPostExecute(List<GroupCategory> result) {
			super.onPostExecute(result);
			mGroupCategories = (ArrayList<GroupCategory>) result;
			final ArrayAdapter<GroupCategory> categoryAdapter = new ArrayAdapter<GroupCategory>(
					CreateGroupActivity.this,
					android.R.layout.simple_spinner_item, mGroupCategories) {

				@Override
				public GroupCategory getItem(int position) {
					return super.getItem(position);
				}

				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					((TextView) v).setTextSize(16);
					((TextView) v).setTypeface(SpikaApp.getTfMyriadPro());
					GroupCategory category = mGroupCategories.get(position);
					((TextView) v).setText(category.getTitle());
					((TextView) v).setTextColor(getResources().getColor(R.color.hookup_positive));
					return v;
				}

				@Override
				public View getDropDownView(int position, View convertView,
						ViewGroup parent) {
					View v = convertView;
					if (v == null) {
						// inflate your customlayout for the textview
						LayoutInflater inflater = CreateGroupActivity.this
								.getLayoutInflater();
						v = inflater.inflate(R.layout.group_category_item,
								parent, false);
					}
					// put the data in it
					GroupCategory category = mGroupCategories.get(position);
					if (category != null) {
						ImageView ivCategoryImage = (ImageView) v
								.findViewById(R.id.ivGroupCategoryImage);
						ProgressBar pbLoading = (ProgressBar) v
								.findViewById(R.id.pbLoadingForImage);
						TextView tvCategoryTitle = (TextView) v
								.findViewById(R.id.tvGroupCategory);
						Utils.displayImage(category.getAvatarFileId(),
								ivCategoryImage, pbLoading, ImageLoader.SMALL,
								R.drawable.image_stub, false);
						tvCategoryTitle.setText(category.getTitle());
					}

					return v;
				}
			};
			
			mSpinnerCategory.setAdapter(categoryAdapter);
			mSpinnerCategory
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {

						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub

						}
					});

		}
	}
}
