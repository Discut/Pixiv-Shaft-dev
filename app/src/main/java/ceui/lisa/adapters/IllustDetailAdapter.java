package ceui.lisa.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.lchad.gifflen.Gifflen;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ceui.lisa.R;
import ceui.lisa.download.FileCreator;
import ceui.lisa.http.ErrorCtrl;
import ceui.lisa.interfaces.OnItemClickListener;
import ceui.lisa.response.IllustsBean;
import ceui.lisa.utils.Channel;
import ceui.lisa.utils.Common;
import ceui.lisa.utils.GlideUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 *
 */
public class IllustDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private OnItemClickListener mOnItemClickListener;
    private IllustsBean allIllust;
    private int imageSize = 0;
    private TagHolder gifHolder;
    private boolean playGif = false;

    public IllustDetailAdapter(IllustsBean list, Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        allIllust = list;
        imageSize = (mContext.getResources().getDisplayMetrics().widthPixels -
                2 * mContext.getResources().getDimensionPixelSize(R.dimen.twelve_dp));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.recy_illust_grid, parent, false);
        return new TagHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final TagHolder currentOne = (TagHolder) holder;

        if (position == 0) {
            ViewGroup.LayoutParams params = currentOne.illust.getLayoutParams();
            params.height = imageSize * allIllust.getHeight() / allIllust.getWidth();
            params.width = imageSize;
            currentOne.illust.setLayoutParams(params);
            Glide.with(mContext)
                    .load(GlideUtil.getLargeImage(allIllust, position))
                    .placeholder(R.color.light_bg)
                    .into(currentOne.illust);

            Common.showLog("height " + params.height + "width " + params.width);

            if (allIllust.getType().equals("ugoira")) {
                gifHolder = currentOne;
                startGif();
            }else {
                currentOne.playGif.setVisibility(View.GONE);
            }
        } else {
            Glide.with(mContext)
                    .asBitmap()
                    .load(GlideUtil.getLargeImage(allIllust, position))
                    .placeholder(R.color.light_bg)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            ViewGroup.LayoutParams params = currentOne.illust.getLayoutParams();
                            params.width = imageSize;
                            params.height = imageSize * resource.getHeight() / resource.getWidth();
                            currentOne.illust.setLayoutParams(params);
                            currentOne.illust.setImageBitmap(resource);
                        }
                    });
        }

        if (mOnItemClickListener != null) {
            currentOne.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(v, position, 0));
            currentOne.playGif.setOnClickListener(v -> {
                currentOne.mProgressBar.setVisibility(View.VISIBLE);
                currentOne.playGif.setVisibility(View.GONE);
                mOnItemClickListener.onItemClick(v, position, 1);
            });
        }
    }

    @Override
    public int getItemCount() {
        return allIllust.getPage_count();
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }

    public static class TagHolder extends RecyclerView.ViewHolder {
        ImageView illust, playGif;
        ProgressBar mProgressBar;

        TagHolder(View itemView) {
            super(itemView);
            illust = itemView.findViewById(R.id.illust_image);
            playGif = itemView.findViewById(R.id.play_gif);
            mProgressBar = itemView.findViewById(R.id.gif_progress);
        }
    }

    public boolean isPlayGif() {
        return playGif;
    }

    public void setPlayGif(boolean playGif) {
        Common.showLog("IllustDetailAdapter 停止播放gif图");
        this.playGif = playGif;
    }

    public void startGif(){
        Common.showLog("IllustDetailAdapter 开始播放gif图");
        playGif = true;
        File parentFile = FileCreator.createGifParentFile(allIllust);
        if (parentFile.exists()) {
            gifHolder.mProgressBar.setVisibility(View.GONE);
            gifHolder.playGif.setVisibility(View.GONE);
            final File[] listfile = parentFile.listFiles();
            Observable.create(new ObservableOnSubscribe<File>() {
                @Override
                public void subscribe(ObservableEmitter<File> emitter) throws Exception {
                    List<File> allFiles = Arrays.asList(listfile);
                    Collections.sort(allFiles, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            if (Integer.valueOf(o1.getName().substring(0, o1.getName().length() - 4)) >
                                    Integer.valueOf(o2.getName().substring(0, o2.getName().length() - 4))) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    int xyz = 0;
                    int count = allFiles.size();
                    while (true) {
                        if (playGif) {
                            emitter.onNext(allFiles.get(xyz % count));
                            Common.showLog(allFiles.get(xyz % count));
                            Thread.sleep(75);
                            xyz++;
                        } else {
                            break;
                        }
                    }
                }
            }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ErrorCtrl<File>() {
                        @Override
                        public void onNext(File s) {
                            Glide.with(mContext)
                                    .load(s)
                                    .placeholder(gifHolder.illust.getDrawable())
                                    .into(gifHolder.illust);
                        }
                    });
        }else {
            gifHolder.playGif.setVisibility(View.VISIBLE);
        }
    }



    /**
     *
     * 生成gif图并且播放
     * 暂时弃用，效果还不如直接用图片做轮播
     *
     * Observable.create(new ObservableOnSubscribe<String>() {
     *                         @Override
     *                         public void subscribe(ObservableEmitter<String> emitter) throws Exception {
     *                             Gifflen mGiffle = new Gifflen.Builder()
     *                                     .delay(75) //每相邻两帧之间播放的时间间隔.
     *                                     .quality(15)
     *                                     .listener(new Gifflen.OnEncodeFinishListener() {  //创建完毕的回调
     *                                         @Override
     *                                         public void onEncodeFinish(String path) {
     *                                             Common.showLog("已保存gif到" + path);
     *                                             emitter.onNext(path);
     *                                         }
     *                                     })
     *                                     .build();
     *                             File resultParent = new File(FileCreator.FILE_GIF_RESULT_PATH);
     *                             if (resultParent.exists()) {
     *
     *                             } else {
     *                                 resultParent.mkdir();
     *                             }
     *
     *                             File file = new File(FileCreator.FILE_GIF_RESULT_PATH, allIllust.getId() + ".gif");
     *                             if (file.exists()) {
     *
     *                             } else {
     *                                 file.createNewFile();
     *                             }
     *                             List<File> allFiles = Arrays.asList(listfile);
     *                             Collections.sort(allFiles, new Comparator<File>() {
     *                                 @Override
     *                                 public int compare(File o1, File o2) {
     *                                     if(Integer.valueOf(o1.getName().substring(0, o1.getName().length() - 4)) >
     *                                             Integer.valueOf(o2.getName().substring(0, o2.getName().length() - 4)))
     *                                     {
     *                                         return 1;
     *                                     }else{
     *                                         return -1;
     *                                     }
     *                                 }
     *                             });
     *                             mGiffle.encode(file.getPath(), allFiles);
     *                         }
     *                     })
     */





}