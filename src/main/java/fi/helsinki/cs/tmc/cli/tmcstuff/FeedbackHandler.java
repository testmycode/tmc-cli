package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackAnswer;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackQuestion;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FeedbackHandler {
    private List<FeedbackQuestion> questions;
    private Io io;
    private static final Logger logger = LoggerFactory.getLogger(FeedbackHandler.class);

    public FeedbackHandler(Io io) {
        this.io = io;
    }

    public Boolean sendFeedback(TmcCore core, List<FeedbackQuestion> questions,
                             URI feedbackUri) {
        this.questions = questions;
        List<FeedbackAnswer> answers = new ArrayList<>();

        for (FeedbackQuestion question : questions) {
            answers.add(getAnswer(question));
        }
        try {
            Boolean call = core.sendFeedback(
                    new TmcCliProgressObserver(io), answers, feedbackUri).call();
            return call;
        } catch (Exception e) {
            logger.error("Couldn't send feedback", e);
        }
        return false;
    }

    private FeedbackAnswer getAnswer(FeedbackQuestion question) {
        if (question.isText()) {
            String wrappedQuestion =
                    "\n# " + WordUtils.wrap("Feedback question: "
                            + question.getQuestion(), 77, "\n# ", true)
                    + "\n#\n# Write your feedback in this file and save it."
                    + "\n# Lines beginning with # are comments and will be ignored.";
            String answer = ExternalsUtil.getUserEditedMessage(wrappedQuestion,
                    "tmc-feedback", true);
            return new FeedbackAnswer(question, answer);
        } else if (question.isIntRange()) {
            io.println("Feedback question: " + question.getQuestion());
            int answer = Integer.MIN_VALUE;
            while (!(question.getIntRangeMin() <= answer && question.getIntRangeMax() >= answer)) {
                try {
                    answer = Integer.parseInt(io.readLine(
                            "[" + question.getIntRangeMin()
                            + "-" + question.getIntRangeMax() + "] "));
                } catch (Exception e) {
                    logger.warn("Couldn't parse string as integer", e);
                }
            }
            return new FeedbackAnswer(question, Integer.toString(answer));
        } else {
            io.println("Feedback question: " + question.getQuestion());
            String answer = io.readLine("Answer: ");
            return new FeedbackAnswer(question, answer);
        }
    }
}
