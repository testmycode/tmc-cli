package fi.helsinki.cs.tmc.cli.shared;

import fi.helsinki.cs.tmc.cli.backend.Account;
import fi.helsinki.cs.tmc.cli.backend.AccountList;
import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.Io;

import fi.helsinki.cs.tmc.core.domain.Course;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is used for searching courses from tmc servers.
 * TODO move some of the test from DownloadExercisesCommand to CourseFinder
 */
public class CourseFinder {

    private final CliContext ctx;
    private Course course;
    private Account account;

    public CourseFinder(CliContext ctx) {
        this.ctx = ctx;
    }

    public Course getCourse() {
        verifySearchIsCalled();
        return course;
    }

    public Account getAccount() {
        verifySearchIsCalled();
        return account;
    }

    public boolean search(String courseName) {
        Io io = ctx.getIo();

        AccountList accountsList = SettingsIo.loadAccountList();
        // LinkedHashMap is used here to preserve ordering.
        Map<Account, Course> matches = new LinkedHashMap<>();

        if (accountsList.getAccountCount() == 0) {
            io.errorln("You haven't logged in on any tmc server.");
            return false;
        }

        for (Account settings : accountsList) {
            ctx.useAccount(settings);
            Course found = TmcUtil.findCourse(ctx, courseName);
            if (found != null) {
                matches.put(settings, found);
            }
        }

        if (matches.isEmpty()) {
            //TODO we could search here for similar courses.
            io.errorln("Course doesn't exist.");
            io.errorln("Please make sure that you are logged in with the right organization.");
            return false;
        } else if (matches.size() == 1) {
            return handleSingleMatchingCourses(matches);
        } else {
            return handleMultipleMatchingCourses(matches);
        }
    }

    private boolean handleSingleMatchingCourses(Map<Account, Course> matches) {
        Map.Entry<Account, Course> firstEntry;
        firstEntry = matches.entrySet().iterator().next();

        this.account = firstEntry.getKey();
        this.course = firstEntry.getValue();
        return true;
    }

    private boolean handleMultipleMatchingCourses(Map<Account, Course> matches) {
        Io io = ctx.getIo();
        io.println("There is " + matches.size() + " courses with same name at different servers.");

        for (Map.Entry<Account, Course> entrySet : matches.entrySet()) {
            Account entryAccount = entrySet.getKey();
            Course entryCourse = entrySet.getValue();

            if (!entryAccount.getUsername().isPresent()) {
                continue;
            }

            if (io.readConfirmation(
                    "Download course from "
                            + entryAccount.getServerAddress()
                            + " with '"
                            + entryAccount.getUsername().get()
                            + "' account",
                    false)) {
                this.account = entryAccount;
                this.course = entryCourse;
                return true;
            }
        }

        io.println("The previous course was last that matched.");
        return false;
    }

    private void verifySearchIsCalled() {
        if (course != null) {
            return;
        }
        throw new RuntimeException("You must search before using getters");
    }
}
