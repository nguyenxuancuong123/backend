package duan.com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD) // AOP khóa phân tán
@Retention(RetentionPolicy.RUNTIME)  // luôn chạy khi chương trình vẫn hoạt động
public @interface DistributedLock {

    String key();
    long waitTime() default 5;
    long lessTime() default 10;

    TimeUnit timeUnit() default TimeUnit.SECONDS;


}
