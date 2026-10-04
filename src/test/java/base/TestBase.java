package base;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.sele3.drivers.DriverRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestBase {

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        DriverRunner.open();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass(){
        DriverRunner.close();
    }
}
