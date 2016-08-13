package fi.helsinki.cs.tmc.cli.shared;

import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
import fi.helsinki.cs.tmc.cli.io.Io;

import fi.helsinki.cs.tmc.core.domain.submission.FeedbackAnswer;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackQuestion;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FeedbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackHandler.class);
    private final CliContext ctx;
    private final Io io;

    private List<FeedbackQuestion> questions;

    public FeedbackHandler(CliContext context) {
        this.ctx = context;
        this.io = context.getIo();
    }

    public boolean sendFeedback(List<FeedbackQuestion> questions, URI feedbackUri) {
        this.questions = questions;
        List<FeedbackAnswer> answers = new ArrayList<>();

        for (FeedbackQuestion question : questions) {
            answers.add(getAnswer(question));
        }
        return TmcUtil.sendFeedback(ctx, answers, feedbackUri);
    }

    private FeedbackAnswer getAnswer(FeedbackQuestion question) {
        if (question.isText()) {
            String wrappedQuestion =
                    "\n# "
                            + WordUtils.wrap(
                                    "Feedback question: " + question.getQuestion(),
                                    77,
                                    "\n# ",
                                    true)
                            + "\n#\n# Write your feedback in this file and save it."
                            + "\n# Lines beginning with # are comments and will be ignored.";
            String answer =
                    ExternalsUtil.getUserEditedMessage(wrappedQuestion, "tmc-feedback", true);
            return new FeedbackAnswer(question, answer);
        } else if (question.isIntRange()) {
            io.println("Feedback question: " + question.getQuestion());
            int answer = Integer.MIN_VALUE;
            while (!(question.getIntRangeMin() <= answer && question.getIntRangeMax() >= answer)) {
                try {
                    answer =
                            Integer.parseInt(
                                    io.readLine(
                                            "["
                                                    + question.getIntRangeMin()
                                                    + "-"
                                                    + question.getIntRangeMax()
                                                    + "] "));
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
