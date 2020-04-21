package com.admin.ui.mvp.presenter;

import com.admin.ui.DialogManager;
import com.admin.ui.mvp.LoginConstants;
import com.admin.ui.mvp.model.LoginFakeModel;
import com.admin.ui.mvp.model.LoginItem;
import com.admin.ui.mvp.model.LoginResultCallback;
import com.admin.ui.mvp.view.LoginViewActivty;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterTest extends TestCase {
    @Mock
    private LoginConstants.View view;

    private LoginConstants.Repository repository;

    private LoginConstants.Presenter presenter;

    @Mock
    private InOrder inOrder;

    @Captor
    private ArgumentCaptor<LoginResultCallback> loginResultListener;

    @Captor
    private ArgumentCaptor<DialogManager.OnDialogCompleted> dialogOnDialogCompletedCallback;

    @Captor
    private ArgumentCaptor<ResetServerInfoResultCallback> resetServerInfoResultCallback;

    @Before
    public void setUp() {
        // 테스트 케이스 시 가짜 reopsitory와 연결
        repository = mock(LoginFakeModel.class);
        presenter = new LoginPresenter(view, repository);
        inOrder = Mockito.inOrder(view, repository);
//        loginResultListener = ArgumentCaptor.forClass(LoginResultCallback.class);
    }

    /**
     * 1. ID/PW 입력 (companyKey포함)
     * 2. 로그인버튼 클릭
     * 3. 입력값 검증 (오류메시지 보여주기)
     * 4. 로딩다이얼로그 보이기 및 키패드 숨김
     * 5. fake Repository와 통신
     * 6. 로그인 성공
     * 7. 입력필드값 초기화
     * 8. 로딩다이얼로그 숨김
     * */
    @Test
    public void testLoginSuccess() {
        LoginItem mockUserInfo = mock(LoginItem.class);

        when(view.getInputID()).thenReturn("admin");
        when(view.getInputPW()).thenReturn("12345");
        when(view.getCompanyKey()).thenReturn("1");

        assertEquals(view.getInputID(), "admin");
        assertEquals(view.getInputPW(), "12345");
        assertEquals(view.getCompanyKey(), "1");

        when(view.isCheckedSavedAdminId()).thenReturn(true);

        presenter.login();

        inOrder.verify(view).showLoadingDialog();
        inOrder.verify(view).hideSoftKeyboard();


        String id = view.getInputID();
        String pwd = view.getInputPW();
        String companyKey = view.getCompanyKey();

        inOrder.verify(repository).doLogin(eq(id),eq(pwd),eq(companyKey), loginResultListener.capture());

        loginResultListener.getValue().onSuccess(mockUserInfo);
        inOrder.verify(repository).setSavedAdminId(anyString());
        inOrder.verify(view).clearInputText();
        inOrder.verify(view).hideLoadingDialog();
    }

    @Test
    public void testResetServer() {
        presenter.clearServerInfo();

        inOrder.verify(view).showClearServerInfoConfirmDialog(dialogOnDialogCompletedCallback.capture());
        dialogOnDialogCompletedCallback.getValue().onCompleted();

        inOrder.verify(repository).resetServerInfo(resetServerInfoResultCallback.capture());

        resetServerInfoResultCallback.getValue().onCompleted();

        inOrder.verify(view, atLeastOnce()).restartApplication();
    }


    /**
     * 1. 잘못된 ID/PW 입력 (companyKey포함)
     * 2. 로그인버튼 클릭
     * 3. 입력값 검증 (오류메시지 보여주기)
     * 4. 로딩다이얼로그 보이기 및 키패드 숨김
     * 5. fake Repository와 통신
     * 6. 로그인 실패
     * 7. 입력필드값 초기화
     * 8. 로딩다이얼로그 숨김
     * 9. 실패팝업 보여주기
     * */
    @Test
    public void testLoginFail() {
        when(view.getInputID()).thenReturn("admin1");
        when(view.getInputPW()).thenReturn("test1");
        when(view.getCompanyKey()).thenReturn("1");

        when(view.isCheckedSavedAdminId()).thenReturn(true);

        assertEquals(view.getInputID(), "admin1");
        assertEquals(view.getInputPW(), "test1");
        assertEquals(view.getCompanyKey(), "1");

        presenter.login();

        inOrder.verify(view).showLoadingDialog();
        inOrder.verify(view).hideSoftKeyboard();


        String id = view.getInputID();
        String pwd = view.getInputPW();
        String companyKey = view.getCompanyKey();

        inOrder.verify(repository).doLogin(eq(id),eq(pwd),eq(companyKey), loginResultListener.capture());

        loginResultListener.getValue().onFail(LoginViewActivty.REQUEST_ERR_MESSAGE, "로그인에 실패하였습니다. id,pw를 정확히 입력해주세요.");
        inOrder.verify(view).clearInputText();

        inOrder.verify(view).showErrorDialog(LoginViewActivty.REQUEST_ERR_MESSAGE, "로그인에 실패하였습니다. id,pw를 정확히 입력해주세요.");
        inOrder.verify(view).hideLoadingDialog();
    }
}
