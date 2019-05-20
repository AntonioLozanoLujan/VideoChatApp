package application.controller.fxml;

import java.util.Optional;

import com.diproject.commons.model.Origin;
import com.diproject.commons.model.User;
import com.diproject.commons.utils.rest.ConfigurationHTTPClient;
import com.diproject.commons.utils.rest.UserHTTPClient;
import com.diproject.commons.utils.ws.WebSocketClient;
import com.sp.dialogs.DialogBuilder;
import com.sp.fxutils.validation.FXUtils;

import application.controller.session.SessionController;
import application.model.ProfileModel;
import application.model.dao.ProfileDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.constants.Styles;

public class RegisterUserController {

	public interface RegisterOperationListener {
		void notifyListener();
	}
	
	@FXML
	private TextField nombreField;
	@FXML
	private TextField apellido1Field;
	@FXML
	private TextField apellido2Field;
	@FXML
	private TextField loginField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private TextField serverField;
	@FXML
	private Label registerWarning;

	private Stage windowStage;
	
	private UserHTTPClient userClient;
	
	private ConfigurationHTTPClient configClient;
	
	private ProfileDAO profileDAO;
	
	private SessionController sc;
	
	private RegisterOperationListener registerListener;

	@FXML
	private void initialize() {
		registerWarning.setText("");
		registerWarning.setId(Styles.Common.warningLabel);
		
		profileDAO = ProfileDAO.getInstance();
		sc = SessionController.getInstance(); 
		userClient = new UserHTTPClient();
		configClient = new ConfigurationHTTPClient();
		
		loginField.textProperty().addListener(
				(observable, oldValue, newValue) -> {	showWrongParams(checkTextFields());  });
		nombreField.textProperty().addListener(
				(observable, oldValue, newValue) -> {	showWrongParams(checkTextFields());  });
		apellido1Field.textProperty().addListener(
				(observable, oldValue, newValue) -> {	showWrongParams(checkTextFields());  });
		apellido2Field.textProperty().addListener(
				(observable, oldValue, newValue) -> {	showWrongParams(checkTextFields());  });
		passwordField.textProperty().addListener(
				(observable, oldValue, newValue) -> {	showWrongParams(checkTextFields());  });
		serverField.textProperty().addListener(
				(observable, oldValue, newValue) -> {	showWrongParams(checkTextFields());  });
	}

	@FXML
	private void registerUser() {
		showFailedRegister(false);
		boolean ok = false;
		boolean validParams = checkTextFields();
		if (validParams) {
			User user = new User();
			user.setUsername(String.format("%s %s %s", nombreField.getText(), apellido1Field.getText(), apellido2Field.getText()));
			user.setLogin(loginField.getText());
			user.setPassword(passwordField.getText());
			try {
				configClient.configureServer(serverField.getText());
				userClient.signup(user);
				sc.setLoggerUser(user);
				sc.setServerAddress(serverField.getText());
				sc.setClient(new WebSocketClient(user.getLogin(), Origin.CHAT));
				registerListener.notifyListener();
				ok = true;
			} catch (Exception e) {
				DialogBuilder.warn().exceptionContent(e).alert().showAndWait();
			}
			
			if(ok) 
				windowStage.close();
		} else {
			showWrongParams(true);
		}
	}
	
	private boolean checkTextFields() {
		boolean validLogin = FXUtils.textfieldTextIsNotNullOrEmpty(loginField);
		boolean validNombre = FXUtils.textfieldTextIsNotNullOrEmpty(nombreField);
		boolean validApellido1= FXUtils.textfieldTextIsNotNullOrEmpty(apellido1Field);
		boolean validApellido2 = FXUtils.textfieldTextIsNotNullOrEmpty(apellido2Field);
		boolean validPassword = FXUtils.textfieldTextIsNotNullOrEmpty(passwordField);
		boolean validServer = FXUtils.textfieldTextIsNotNullOrEmpty(serverField);
		
		return validLogin && validNombre && validApellido1 && validApellido2 && validPassword && validServer;
	}
	
	private void showWrongParams(boolean show) {
		String warn = "Por favor rellene todos los campos";
		registerWarning.setText(show ? warn : "");
	}
	
	private void showFailedRegister(boolean show) {
		String warn = "Ha habido un problema a la hora de registrarse en el servidor";
		registerWarning.setText(show ? warn : "");
	}

	public void setWindowStage(Stage stage) {
		this.windowStage = stage;
	}

	public void setOnSuccessfulRegister(RegisterOperationListener listener) {
		registerListener = listener;
	}
}
