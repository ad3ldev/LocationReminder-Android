import android.view.WindowManager
import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

// A class used to create a custom toast matcher.
class ToastMatcher : TypeSafeMatcher<Root?>() {

    override fun describeTo(description: Description?) {
        description?.appendText("is toast");
    }
    override fun matchesSafely(root: Root?): Boolean {
        // Check the the type of the element on the screen
        val type = root?.windowLayoutParams?.get()?.type;
        // if it's toast then return true
        if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
            val windowToken = root.decorView.windowToken;
            val appToken = root.decorView.applicationWindowToken;
            if (windowToken == appToken) {
                return true;
            }
        }
        return false;
    }
}