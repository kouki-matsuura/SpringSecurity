package com.example.domain.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import com.example.domain.model.AppUserDetails;


@Repository
public class LoginUserRepository {

	@Autowired
	private JdbcTemplate jdbc;
	
	//ユーザ情報を取得するSQL
	private static final String SELECT_USER_SQL = "SELECT *"
			+ " FROM m_user"
			+ " WHERE user_id= ?";
	
	//権限リストを取得するSQL
	private static final String SELECT_USER_ROLE_SQL = "SELECT"
			+ " role.role_name"
			+ " FROM"
			+ " m_user"
			+ " INNER JOIN"
			+ " t_user_role user_role"
			+ " ON"
			+ " m_user.user_id = user_role.user_id"
			+ " INNER JOIN"
			+ " m_role role"
			+ " ON user_role.role_id = role.role_id"
			+ " WHERE m_user.user_id = ?";
	
	//パスワードと期限を更新するSQL
	private static final String UPDATE_PASSWORD_SQL = 
			"UPDATE m_user"
			+ " SET password=?,"
			+ " pass_update_date = ?"
			+ " WHERE user_id = ?";
	
	//ログイン失敗回数と有効・無効フラグを更新するSQL
	private static final String UPDATE_LOCK_SQL =
			"UPDATE m_user"
			+ " SET login_miss_times = ?,"
			+ " unlock = ?"
			+ " WHERE user_id = ?";
	
	//ユーザー情報を取得してUserDetailsを生成するメソッド
	public UserDetails selectOne(String userId) {
		//select実行(ユーザーの取得)
		Map<String, Object> userMap = 
				jdbc.queryForMap(SELECT_USER_SQL, userId);
		
		//権限リストの取得(メソッド)
		List<GrantedAuthority> grantedAuthorityList = getRoleList(userId);
		
		//結果返却用のUserDetailsを生成
		AppUserDetails user = buildUserDetails(userMap, grantedAuthorityList);
		
		return user;
	}
	
	//権限リストを取得するメソッド
	private List<GrantedAuthority> getRoleList(String userId) {
		//select実行
		List<Map<String, Object>> authorityList =
				jdbc.queryForList(SELECT_USER_ROLE_SQL, userId);
		
		//結果返却用のList生成
		List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
		
		for (Map<String, Object> map: authorityList) {
			//ロール名を取得
			String roleName = (String) map.get("role_name");
			//SimplaGrantedAuthorityインスタンスの生成
			GrantedAuthority authority = new SimpleGrantedAuthority(roleName);
			grantedAuthorityList.add(authority);
		}
		return grantedAuthorityList;
	}
	
	//ユーザークラスの作成
	private AppUserDetails buildUserDetails(Map<String, Object> userMap,
			List<GrantedAuthority> grantedAuthorityList) {
		//Mapから値取得
		String userId = (String) userMap.get("user_id");
		String password = (String) userMap.get("password");
		Date passUpdateDate = (Date) userMap.get("pass_update_date");
		int loginMissTimes = (Integer) userMap.get("login_miss_times");
		boolean unlock = (Boolean) userMap.get("unlock");
		boolean enabled = (Boolean) userMap.get("enabled");
		Date userDueDate = (Date) userMap.get("user_due_date");
		String tenantId = (String) userMap.get("tenant_id");
		String appUserName = (String) userMap.get("user_name");
		String mailAddress = (String) userMap.get("mail_address");
		
		//練習返却用のUserDetailsを生成
		AppUserDetails user = new AppUserDetails()
				.builder()
					.userId(userId)
					.password(password)
					.passUpdateDate(passUpdateDate)
					.loginMissTimes(loginMissTimes)
					.unlock(unlock)
					.enabled(enabled)
					.userDueDate(userDueDate)
					.tenantId(tenantId)
					.appUserName(appUserName)
					.mailAddress(mailAddress)
					.authority(grantedAuthorityList)
					.build();
		
		return user;
		
	}
	
	//パスワードと期限を更新するメソッド
	public int updatePassword(String userId, String password, Date passwordUpdateDate) {
		//パスワード更新
		int result = jdbc.update(UPDATE_PASSWORD_SQL, password, passwordUpdateDate, userId);
		return result;
	}
	
	//ログイン失敗回数と有効・無効フラグを更新する
	public int updateUnlock(String userId, int loginMissTime, boolean unlock) {
		//失敗回数の更新
		int result = jdbc.update(UPDATE_LOCK_SQL, loginMissTime, unlock, userId);
		
		return result;
	}
}
