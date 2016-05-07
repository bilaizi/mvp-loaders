package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.data.Task;

/**
 * Custom {@link android.content.Loader} for a {@link Task},
 * using the {@link TasksRepository} as its source.
 * This Loader is a {@link AsyncTaskLoader}
 * so it queries the data asynchronously.
 */
public class TaskLoader extends AsyncTaskLoader<Task>
        implements TasksRepository.TasksRepositoryObserver {
    private final String mTaskId;
    private TasksRepository mRepository;
    public TaskLoader(String taskId, Context context) {
        super(context);
        this.mTaskId = taskId;
        mRepository = Injection.provideTasksRepository(context);
    }
    @Override
    public Task loadInBackground() {return mRepository.getTask(mTaskId);}
    @Override
    public void deliverResult(Task data) {
        if (isReset()) {return;}
        if (isStarted()) {super.deliverResult(data);}
    }

    @Override
    protected void onStartLoading() {
        // Deliver any previously loaded data immediately if available.
        if (mRepository.cachedTasksAvailable()) {
            deliverResult(mRepository.getCachedTask(mTaskId));
        }
        // Begin monitoring the underlying data source.
        mRepository.addContentObserver(this);
        if (takeContentChanged() || !mRepository.cachedTasksAvailable()) {
            // When a change has  been delivered or the repository cache isn't available, we force
            // a load.
            forceLoad();
        }
    }

    @Override
    protected void onReset() {
        onStopLoading();
        mRepository.removeContentObserver(this);
    }

    @Override
    public void onTasksChanged() {
        if (isStarted()) {
            forceLoad();
        }
    }
}
