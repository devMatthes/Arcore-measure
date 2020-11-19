# Arcore-measue

## Project Overview 🎉
With this application you can easliy estimate how much paint you need.

Move your device from left to right to detect a plane. Set the aim at the interesting point and just click to start the measure. When you end you can save the measure on your device to have an access to saved records in future.
## Tech/framework used 🔧

| Tech                                                    ||
| ------------------------------------------------------- |-------------------------------------------------------|
| Google ARCore                          | v1.0  |
| Sceneform                              | v1.15.0|

## Code Example 🔍
### Simulating an aim movement
```java
private void touchScreenCenterConstantly() {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 10;

        float x = (float)(this.getResources().getDisplayMetrics().widthPixels) / 2;
        float y = (float)(this.getResources().getDisplayMetrics().heightPixels) / 2;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                0
        );
        arFragment.getArSceneView().dispatchTouchEvent(motionEvent);
    }
```
```java
private void refreshAim(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (aimRender == null)
            return;

        if (motionEvent.getMetaState() == 0) {
            if (anchorNodeTemp != null)
                anchorNodeTemp.getAnchor().detach();

            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
            transformableNode.setRenderable(aimRender);
            transformableNode.setParent(anchorNode);
            arFragment.getArSceneView().getScene().addOnUpdateListener(this);
            arFragment.getArSceneView().getScene().addChild(anchorNode);
            anchorNodeTemp = anchorNode;
        }
    }
```
touchScreenCenterConstantly() function imitates constant tapping the middle of the screen in combination with refreshAim() function which attaches/detaches aim render to/from the point "tapped" by touchScreenCenterConstantly(), simulates the crosshair movement.

### Putting virtual points (renders) in "real world" based on crosshair position.
```java
public void addFromAim(View view) {
        if (anchorNodeTemp != null) {
            if (currentAnchorNode.size() == 0 && roomHeightConfirm == 0) {
                btnAdd.setForeground(getDrawable(R.drawable.ic_upload));
                btnAdd.setOnClickListener(null);
                heightMeasurement();
            }

            Vector3 worldPosition = anchorNodeTemp.getWorldPosition();
            Quaternion worldRotation = anchorNodeTemp.getWorldRotation();

            // SETTING A VIRTUAL POINT BASED ON "REAL WORLD" COORDINATES
            worldPosition.x += 0.0000001f;
            AnchorNode confirmedAnchorNode = new AnchorNode();
            confirmedAnchorNode.setWorldPosition(worldPosition);
            confirmedAnchorNode.setWorldRotation(worldRotation);
            Anchor anchor = confirmedAnchorNode.getAnchor();
            confirmedAnchorNode.setParent(arFragment.getArSceneView().getScene());
            TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
            transformableNode.setRenderable(pointRender);
            transformableNode.setParent(confirmedAnchorNode);
            arFragment.getArSceneView().getScene().addOnUpdateListener(this);
            arFragment.getArSceneView().getScene().addChild(confirmedAnchorNode);
            currentAnchor.add(anchor);
            currentAnchorNode.add(confirmedAnchorNode);

            if (currentAnchorNode.size() >= 2) {

                Vector3 node1Pos = currentAnchorNode.get(currentAnchorNode.size() - 2).getWorldPosition();
                Vector3 node2Pos = currentAnchorNode.get(currentAnchorNode.size() - 1).getWorldPosition();
                difference = Vector3.subtract(node1Pos, node2Pos);
                totalLength += difference.length();

                final Quaternion rotationFromAToB =
                        Quaternion.lookRotation(difference.normalized(), Vector3.up());

                // LINKING TWO POINTS WITH VIRTUAL LINE
                AnchorNode lineBetween = new AnchorNode();
                lineBetween.setParent(arFragment.getArSceneView().getScene());
                lineBetween.setWorldPosition(Vector3.add(node1Pos, node2Pos).scaled(.5f));
                lineBetween.setWorldRotation(rotationFromAToB);
                lineBetween.setLocalScale(new Vector3(1f, 1f, difference.length()));
                TransformableNode lineNode = new TransformableNode(arFragment.getTransformationSystem());
                lineNode.setParent(lineBetween);
                lineNode.setRenderable(widthLineRender);

                // ATTACHING MEASURE LABEL TO CREATED LINE
                AnchorNode lengthLabel = new AnchorNode();
                lengthLabel.setParent(arFragment.getArSceneView().getScene());
                lengthLabel.setWorldPosition(Vector3.add(node1Pos, node2Pos).scaled(.5f));
                TransformableNode distanceNode = new TransformableNode(arFragment.getTransformationSystem());
                distanceNode.setParent(lengthLabel);
                initTextBox(difference.length(), distanceNode);
                labelArray.add(lengthLabel);
                btnSave.setEnabled(true);
            }
        }
    }
```

```java
    @SuppressLint("ClickableViewAccessibility")
    private void heightMeasurement() {
        btnAdd.setOnTouchListener(new View.OnTouchListener() {
        private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (anchorNodeHeight == null) {
                    btnSave.setEnabled(true);
                    anchorNodeHeight = currentAnchorNode.get(0);
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        roomHeight = 0;
                        mHandler.postDelayed(mAction, 20);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        anchorNodeHeight = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
                @Override public void run() {
                    if (anchorNodeHeight!=null) {
                        Vector3 worldPosition = anchorNodeHeight.getWorldPosition();
                        Quaternion worldRotation = anchorNodeHeight.getWorldRotation();
                        if (roomHeight == 0) {
                            AnchorNode stand = new AnchorNode();
                            worldPosition.x += 0.0000001f;
                            stand.setWorldPosition(worldPosition);
                            stand.setWorldRotation(worldRotation);
                            stand.setParent(arFragment.getArSceneView().getScene());
                            TransformableNode tN = new TransformableNode(arFragment.getTransformationSystem());
                            tN.setRenderable(aimRender);
                            tN.setParent(stand);
                            arFragment.getArSceneView().getScene().addOnUpdateListener(MainActivity.this);
                            arFragment.getArSceneView().getScene().addChild(stand);
                        }
                        worldPosition.y += 0.01f;
                        AnchorNode anchorNode = new AnchorNode();
                        anchorNode.setWorldPosition(worldPosition);
                        anchorNode.setWorldRotation(worldRotation);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());
                        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                        transformableNode.setRenderable(heightLineRender);
                        transformableNode.setParent(anchorNode);

                        roomHeight += 0.01f;
                        if (roomHeight < 1f)
                            tvDistance.setText(String.format(Locale.ENGLISH, "%.0f", roomHeight*100) + " cm");
                        else
                            tvDistance.setText(String.format(Locale.ENGLISH, "%.2f", roomHeight) + " m");

                        anchorNodeHeight = anchorNode;
                        arFragment.getArSceneView().getScene().addOnUpdateListener(MainActivity.this);
                        arFragment.getArSceneView().getScene().addChild(anchorNode);
                        mHandler.postDelayed(this, 20);
                    }
                }
            };
        });
    }
```
Firstly you put one point that activates heightMeasurement() function then you put how many points you want which reflect the width(s) of the wall(s) and after confirmation the measure of selected surface is being calculated.
## Requirements 💾
### Hardware
An Android device must be running Android 7.0 or newer

### Software
Android SDK 7.0 (API Level 24) or later, installed using the SDK Manager in Android Studio
