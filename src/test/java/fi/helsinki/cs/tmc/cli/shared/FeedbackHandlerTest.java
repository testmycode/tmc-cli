package fi.helsinki.cs.tmc.cli.shared;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackAnswer;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackQuestion;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ExternalsUtil.class, TmcUtil.class})
public class FeedbackHandlerTest {
    private List<FeedbackQuestion> questions;
    private String a1;
    private String a2;
    private String a3;
    private TestIo io;
    private CliContext ctx;
    @Captor ArgumentCaptor<List<FeedbackAnswer>> answerCaptor;

    @Before
    public void setup() {
        a1 = a2 = a3 = "";
        mockStatic(ExternalsUtil.class);
        mockStatic(TmcUtil.class);
        when(ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean()))
                .thenReturn("you're programm sucks");
        FeedbackQuestion q1 = new FeedbackQuestion();
        q1.setQuestion("What's your opinion on TMC-CLI?");
        q1.setKind("text");
        FeedbackQuestion q2 = new FeedbackQuestion();
        q2.setQuestion("Please rate this program.");
        q2.setKind("intrange\\[0\\.\\.4]");
        FeedbackQuestion q3 = new FeedbackQuestion();
        q3.setQuestion("This type of question doesn't really exist but whatever");
        q3.setKind("misc");
        questions = Arrays.asList(q1, q2, q3);
        io = new TestIo();
        ctx = mock(CliContext.class);
        Mockito.when(ctx.getIo()).thenReturn(io);
    }

    @Test
    public void sendingFeedbackWorks() {
        PowerMockito.when(
                TmcUtil.sendFeedback(any(CliContext.class), any(List.class), any(URI.class)))
                .thenReturn(true);
        io.addLinePrompt("1");
        io.addLinePrompt("who cars");
        io.addConfirmationPrompt(true);
        FeedbackHandler handler = new FeedbackHandler(ctx);
        handler.sendFeedback(questions, URI.create("https://eeeeeeeeeee.com"));

        verifyStatic();
        TmcUtil.sendFeedback(any(CliContext.class), answerCaptor.capture(), any(URI.class));
        List<FeedbackAnswer> answers = answerCaptor.getValue();
        assertThat(answers.get(0).getQuestion().getQuestion()).isEqualTo(
                "What's your opinion on TMC-CLI?");
        assertThat(answers.get(0).getAnswer()).isEqualTo("you're programm sucks");
        io.assertContains("Please rate this program.");
        assertThat(answers.get(1).getQuestion().getQuestion()).isEqualTo(
                "Please rate this program.");
        assertThat(answers.get(1).getAnswer()).isEqualTo("1");
        io.assertContains(
                "Feedback question: This type of question doesn't really exist but whatever");
        assertThat(answers.get(2).getQuestion().getQuestion()).isEqualTo(
                "This type of question doesn't really exist but whatever");
        assertThat(answers.get(2).getAnswer()).isEqualTo("who cars");
    }
}
