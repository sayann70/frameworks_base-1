/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.dagger;

import com.android.systemui.BootCompleteCacheImpl;
import com.android.systemui.CoreStartable;
import com.android.systemui.Dependency;
import com.android.systemui.InitController;
import com.android.systemui.SystemUIAppComponentFactory;
import com.android.systemui.dagger.qualifiers.PerUser;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.KeyguardSliceProvider;
import com.android.systemui.media.muteawait.MediaMuteAwaitConnectionCli;
import com.android.systemui.media.nearby.NearbyMediaDevicesManager;
import com.android.systemui.media.taptotransfer.MediaTttCommandLineHelper;
import com.android.systemui.media.taptotransfer.receiver.MediaTttChipControllerReceiver;
import com.android.systemui.media.taptotransfer.sender.MediaTttChipControllerSender;
import com.android.systemui.people.PeopleProvider;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.unfold.FoldStateLogger;
import com.android.systemui.unfold.FoldStateLoggingProvider;
import com.android.systemui.unfold.SysUIUnfoldComponent;
import com.android.systemui.unfold.UnfoldLatencyTracker;
import com.android.systemui.unfold.util.NaturalRotationUnfoldProgressProvider;
import com.android.wm.shell.ShellCommandHandler;
import com.android.wm.shell.TaskViewFactory;
import com.android.wm.shell.apppairs.AppPairs;
import com.android.wm.shell.back.BackAnimation;
import com.android.wm.shell.bubbles.Bubbles;
import com.android.wm.shell.compatui.CompatUI;
import com.android.wm.shell.displayareahelper.DisplayAreaHelper;
import com.android.wm.shell.draganddrop.DragAndDrop;
import com.android.wm.shell.hidedisplaycutout.HideDisplayCutout;
import com.android.wm.shell.legacysplitscreen.LegacySplitScreen;
import com.android.wm.shell.onehanded.OneHanded;
import com.android.wm.shell.pip.Pip;
import com.android.wm.shell.recents.RecentTasks;
import com.android.wm.shell.splitscreen.SplitScreen;
import com.android.wm.shell.startingsurface.StartingSurface;
import com.android.wm.shell.tasksurfacehelper.TaskSurfaceHelper;
import com.android.wm.shell.transition.ShellTransitions;

import com.google.android.systemui.smartspace.KeyguardSmartspaceController;

import java.util.Map;
import java.util.Optional;

import javax.inject.Provider;

import dagger.BindsInstance;
import dagger.Subcomponent;

/**
 * Dagger Subcomponent for Core SysUI.
 */
@SysUISingleton
@Subcomponent(modules = {
        DefaultComponentBinder.class,
        DependencyProvider.class,
        SystemUIBinder.class,
        SystemUIModule.class,
        SystemUICoreStartableModule.class,
        ReferenceSystemUIModule.class})
public interface SysUIComponent {

    /**
     * Builder for a SysUIComponent.
     */
    @SysUISingleton
    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        Builder setPip(Optional<Pip> p);

        @BindsInstance
        Builder setLegacySplitScreen(Optional<LegacySplitScreen> s);

        @BindsInstance
        Builder setSplitScreen(Optional<SplitScreen> s);

        @BindsInstance
        Builder setAppPairs(Optional<AppPairs> s);

        @BindsInstance
        Builder setOneHanded(Optional<OneHanded> o);

        @BindsInstance
        Builder setBubbles(Optional<Bubbles> b);

        @BindsInstance
        Builder setTaskViewFactory(Optional<TaskViewFactory> t);

        @BindsInstance
        Builder setHideDisplayCutout(Optional<HideDisplayCutout> h);

        @BindsInstance
        Builder setShellCommandHandler(Optional<ShellCommandHandler> shellDump);

        @BindsInstance
        Builder setTransitions(ShellTransitions t);

        @BindsInstance
        Builder setStartingSurface(Optional<StartingSurface> s);

        @BindsInstance
        Builder setDisplayAreaHelper(Optional<DisplayAreaHelper> h);

        @BindsInstance
        Builder setTaskSurfaceHelper(Optional<TaskSurfaceHelper> t);

        @BindsInstance
        Builder setRecentTasks(Optional<RecentTasks> r);

        @BindsInstance
        Builder setCompatUI(Optional<CompatUI> s);

        @BindsInstance
        Builder setDragAndDrop(Optional<DragAndDrop> d);

        @BindsInstance
        Builder setBackAnimation(Optional<BackAnimation> b);

        SysUIComponent build();
    }

    /**
     * Initializes all the SysUI components.
     */
    default void init() {
        // Initialize components that have no direct tie to the dagger dependency graph,
        // but are critical to this component's operation
        // TODO(b/205034537): I think this is a good idea?
        getSysUIUnfoldComponent().ifPresent(c -> {
            c.getUnfoldLightRevealOverlayAnimation().init();
            c.getUnfoldTransitionWallpaperController().init();
        });
        getNaturalRotationUnfoldProgressProvider().ifPresent(o -> o.init());
        // No init method needed, just needs to be gotten so that it's created.
        getMediaTttChipControllerSender();
        getMediaTttChipControllerReceiver();
        getMediaTttCommandLineHelper();
        getMediaMuteAwaitConnectionCli();
        getNearbyMediaDevicesManager();
        getUnfoldLatencyTracker().init();
        getFoldStateLoggingProvider().ifPresent(FoldStateLoggingProvider::init);
        getFoldStateLogger().ifPresent(FoldStateLogger::init);
    }

    /**
     * Provides a BootCompleteCache.
     */
    @SysUISingleton
    BootCompleteCacheImpl provideBootCacheImpl();

    /**
     * Creates a ContextComponentHelper.
     */
    @SysUISingleton
    ConfigurationController getConfigurationController();

    /**
     * Creates a ContextComponentHelper.
     */
    @SysUISingleton
    ContextComponentHelper getContextComponentHelper();

    /**
     * Creates a UnfoldLatencyTracker.
     */
    @SysUISingleton
    UnfoldLatencyTracker getUnfoldLatencyTracker();

    /**
     * Creates a FoldStateLoggingProvider.
     */
    @SysUISingleton
    Optional<FoldStateLoggingProvider> getFoldStateLoggingProvider();

    /**
     * Creates a FoldStateLogger.
     */
    @SysUISingleton
    Optional<FoldStateLogger> getFoldStateLogger();

    /**
     * Main dependency providing module.
     */
    @SysUISingleton
    Dependency createDependency();

    /** */
    @SysUISingleton
    DumpManager createDumpManager();

    /**
     * Creates a InitController.
     */
    @SysUISingleton
    InitController getInitController();

    /**
     * For devices with a hinge: access objects within this component
     */
    Optional<SysUIUnfoldComponent> getSysUIUnfoldComponent();

    /**
     * For devices with a hinge: the rotation animation
     */
    Optional<NaturalRotationUnfoldProgressProvider> getNaturalRotationUnfoldProgressProvider();

    /** */
    Optional<MediaTttChipControllerSender> getMediaTttChipControllerSender();

    /** */
    Optional<MediaTttChipControllerReceiver> getMediaTttChipControllerReceiver();

    /** */
    Optional<MediaTttCommandLineHelper> getMediaTttCommandLineHelper();

    /** */
    Optional<MediaMuteAwaitConnectionCli> getMediaMuteAwaitConnectionCli();

    /** */
    Optional<NearbyMediaDevicesManager> getNearbyMediaDevicesManager();

    /**
     * Returns {@link CoreStartable}s that should be started with the application.
     */
    Map<Class<?>, Provider<CoreStartable>> getStartables();

    /**
     * Returns {@link CoreStartable}s that should be started for every user.
     */
    @PerUser Map<Class<?>, Provider<CoreStartable>> getPerUserStartables();

    /**
     * Member injection into the supplied argument.
     */
    void inject(SystemUIAppComponentFactory factory);

    /**
     * Member injection into the supplied argument.
     */
    void inject(KeyguardSliceProvider keyguardSliceProvider);

    /**
     * Member injection into the supplied argument.
     */
    void inject(PeopleProvider peopleProvider);

    /**
     * Creates a KeyguardSmartspaceController.
     */
    @SysUISingleton
    KeyguardSmartspaceController createKeyguardSmartspaceController();
}
