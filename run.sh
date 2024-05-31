throughtput=$1
interations=$2
time=$3

java --illegal-access=permit \
--add-exports=java.base/jdk.internal.util=ALL-UNNAMED \
--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED \
--add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
--add-opens=java.base/java.io=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
-Dthroughput=$throughtput \
-Dinterations=$interations \
-Dtime=$time \
-DenableTailerAffinity \
-DenableAppenderAffinity \
-jar target/benchmark-queue-1.jar