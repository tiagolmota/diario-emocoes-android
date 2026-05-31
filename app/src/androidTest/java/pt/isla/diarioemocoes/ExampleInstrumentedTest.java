package pt.isla.diarioemocoes;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Testes de instrumentação — executam no dispositivo/emulador.
 * Para executar: clique direito → "Run 'ExampleInstrumentedTest'"
 * ou no terminal: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("pt.isla.diarioemocoes", appContext.getPackageName());
    }
}
