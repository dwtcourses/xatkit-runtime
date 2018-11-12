package fr.zelus.jarvis.plugins.github.module.action;

import com.jcabi.github.Coordinates;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import fr.inria.atlanmod.commons.log.Log;
import fr.zelus.jarvis.core.JarvisAction;
import fr.zelus.jarvis.core.JarvisException;
import fr.zelus.jarvis.core.session.JarvisSession;
import fr.zelus.jarvis.plugins.github.module.GithubModule;

import java.io.IOException;

import static fr.inria.atlanmod.commons.Preconditions.checkNotNull;
import static java.util.Objects.isNull;

/**
 * A {@link JarvisAction} that opens a new issue on the given {@code repository} with the provided {@code issueTitle}
 * and {@code issueContent}.
 * <p>
 * This class relies on the {@link GithubModule} to access the Github API and authenticate the bot.
 * <p>
 * <b>Note: </b> this class requires that its containing {@link GithubModule} has been loaded with valid Github
 * credentials in order to authenticate the bot and access the Github API.
 *
 * @see GithubModule
 */
public class OpenIssue extends JarvisAction<GithubModule> {

    /**
     * The Github user managing the repository to open an issue in.
     */
    private String user;

    /**
     * The Github repository to open an issue in.
     */
    private String repository;

    /**
     * The title of the issue to open.
     */
    private String issueTitle;

    /**
     * The content of the issue to open.
     */
    private String issueContent;

    /**
     * Constructs a new {@link OpenIssue} with the provided {@code containingModule}, {@code session}, {@code user},
     * {@code repository}, {@code issueTitle}, and {@code issueContent}.
     * <p>
     * This constructor accepts {@code null} as its {@code issueContent} parameter. In this case the opened issue
     * will have an empty content.
     *
     * @param containingModule the {@link GithubModule} containing this action
     * @param session          the {@link JarvisSession} associated to this action
     * @param user             the Github user managing the repository to open an issue in
     * @param repository       the Github repository to open an issue in
     * @param issueTitle       the title of the issue to open
     * @param issueContent     the content of the issue to open
     * @throws NullPointerException if the provided {@code containingModule}, {@code session}, {@code user}, {@code
     *                              repository}, or {@code issueTitle} is {@code null}
     */
    public OpenIssue(GithubModule containingModule, JarvisSession session, String user, String repository, String
            issueTitle, String issueContent) {
        super(containingModule, session);
        checkNotNull(user, "Cannot construct a %s action with the provided Github user %s", this.getClass()
                .getSimpleName(), user);
        checkNotNull(repository, "Cannot construct a %s action with the provided Github repository %s", this.getClass
                ().getSimpleName(), repository);
        checkNotNull(issueTitle, "Cannot construct a %s action with the provided issue title %s", this.getClass()
                .getSimpleName(), issueTitle);
        this.user = user;
        this.repository = repository;
        this.issueTitle = issueTitle;
        this.issueContent = issueContent;
        if (isNull(issueContent)) {
            Log.warn("{0} initialized with an empty issue content", this.getClass().getSimpleName());
        }
    }

    /**
     * Opens a new issue on the given {@code repository} with the provided {@code issueTitle} and {@code issueContent}.
     * <p>
     * This method relies on the containing {@link GithubModule} to access the Github API, and will throw a
     * {@link JarvisException} if the Jarvis {@link org.apache.commons.configuration2.Configuration} does not define
     * valid Github authentication credentials.
     *
     * @return the created {@link Issue}
     * @throws JarvisException if the {@link GithubModule} does not hold a valid Github API client (i.e. if the
     *                         Jarvis {@link org.apache.commons.configuration2.Configuration} does not define valid
     *                         Github authentication credentials)
     * @see GithubModule#getGithubClient()
     */
    @Override
    protected Object compute() {
        Repo githubRepo = this.module.getGithubClient().repos().get(new Coordinates.Simple(user, repository));
        try {
            Issue githubIssue = githubRepo.issues().create(issueTitle, issueContent);
            return githubIssue;
        } catch (IOException e) {
            throw new JarvisException("Cannot open the Github issue, see attached exception", e);
        }
    }
}
