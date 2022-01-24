<?php

require_once('for_php7.php');

class knja110_2Query extends Query {

    //起動チェック
    function ChecktoStart($db)
    {
        foreach (array('Z002','H201','A020') as $val) //性別、続柄、地区がマスタに存在するかチェック！
        {
            $query = "SELECT COUNT(*) AS a FROM v_name_mst WHERE namecd1 = '".$val."' AND year = '".CTRL_YEAR."'";
            if ($db->getOne($query) == "0") {
                return false;
            }
        }
        return true; 
    }

    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($model){
        $query  ="SELECT schregno,name_show as name FROM schreg_base_mst";
        $query .=" WHERE schregno = '".$model->schregno."'";
        return $query;
    }

    //学籍住所データよりデータを取得
    function getAddress_all($model)
    {
        return "SELECT * FROM schreg_address_dat WHERE schregno = '".$model->schregno."'";
    }

    //listの地区コード表示
    function List_AreaCd($cd)
    {
        return "SELECT name1 FROM v_name_mst WHERE namecd1 = 'A020' AND namecd2 = '".$cd."' AND year = '".CTRL_YEAR."'";
    }

    //期間開始日付のリンクをクリックした学籍住所データを単票形式で表示
    function getRow_Address($schregno,$issuedate)
    {
        $query  = "";
        $query .= " SELECT * FROM SCHREG_ADDRESS_DAT ";
        $query .= "  WHERE SCHREGNO  = '".$schregno."' AND";
        if ($issuedate!=""){
            $query .= "      ISSUEDATE = date('".str_replace("/", "-", $issuedate)."') ";
        }else{
            $query .= "      ISSUEDATE IS NULL ";
        }
        return $query;
    }

    //保護者情報の取得
    function getGuardianAddr($schregno)
    {
        $query  = "SELECT relationship, ";
        $query .= "       guard_name, ";
        $query .= "       guard_kana, ";
        $query .= "       guard_sex, ";
        $query .= "       guard_birthday, ";
        $query .= "       guard_zipcd, ";
        $query .= "       guard_addr1, ";
        $query .= "       guard_addr2, ";
        $query .= "       guard_telno, ";
        $query .= "       guard_faxno, ";
        $query .= "       guard_e_mail, ";
        $query .= "       guard_jobcd, ";
        $query .= "       guard_work_name, ";
        $query .= "       guard_work_telno ";
        $query .= "  FROM guardian_dat ";
        $query .= " WHERE schregno = '".$schregno."'";
        return $query;
    }

    //コンボボックスの地区コードを表示
    function getV_name_mst()
    {
        return "SELECT namecd2,name1 FROM v_name_mst WHERE namecd1 ='A020' AND year = '".CTRL_YEAR."'";
    }

    //コンボボックスのデータを名称マスタから取得
    function get_name_mst(){

        $query  = "SELECT namecd1, ";
        $query .= "       namecd2, ";
        $query .= "       name1 ";
        $query .= "  FROM v_name_mst";
        $query .= " WHERE namecd1 IN ('Z002','H201','H202') ";
        $query .= "   AND year = '".CTRL_YEAR."'";
        $query .= "ORDER BY namecd1,namecd2";
        return $query;
    }
    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

#        $data["SCHREGNO"][TEXT]       = $model->schregno;
        $data["SCHREGNO"][TEXT]       = substr("00000000".$model->schregno,strlen($model->schregno),8);
        $data["ISSUEDATE"][FUNC]      = "date('".$model->field["ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]     = "date('".$model->field["EXPIREDATE"]."')";
        $data["ZIPCD"][TEXT]          = $model->field["ZIPCD"];
        $data["AREACD"][TEXT]         = $model->field["AREACD"];
        $data["ADDR1"][TEXT]          = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]          = $model->field["ADDR2"];
        $data["ADDR1_ENG"][TEXT]      = $model->field["ADDR1_ENG"];
        $data["ADDR2_ENG"][TEXT]      = $model->field["ADDR2_ENG"];
        $data["TELNO"][TEXT]          = $model->field["TELNO"];
        $data["FAXNO"][TEXT]          = $model->field["FAXNO"];
        $data["EMAIL"][TEXT]          = $model->field["EMAIL"];

/*コメント2005/10/20 */
#        $data["EMERGENCYCALL"][TEXT]        = $model->field["EMERGENCYCALL"];
#        $data["EMERGENCYTELNO"][TEXT]       = $model->field["EMERGENCYTELNO"];
/*コメントおわり */

        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data,"schreg_address_dat");
        $db->query($query);

        //保護者データを作成
        knja110_2Query::update_Guardian_dat($db, $model);

        //急用連絡先を更新(2005/10/20 追加)
        knja110_2Query::updateEmergencyInfo($db, $model, "update");

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateModel($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["ISSUEDATE"][FUNC]            = "date('".$model->field["ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]           = "date('".$model->field["EXPIREDATE"]."')";
        $data["ZIPCD"][TEXT]                = $model->field["ZIPCD"];
        $data["AREACD"][TEXT]               = $model->field["AREACD"];
        $data["ADDR1"][TEXT]                = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]                = $model->field["ADDR2"];
        $data["ADDR1_ENG"][TEXT]            = $model->field["ADDR1_ENG"];
        $data["ADDR2_ENG"][TEXT]            = $model->field["ADDR2_ENG"];
        $data["TELNO"][TEXT]                = $model->field["TELNO"];
        $data["FAXNO"][TEXT]                = $model->field["FAXNO"];
        $data["EMAIL"][TEXT]                = $model->field["EMAIL"];

/*コメント2005/10/20 */
#        $data["EMERGENCYCALL"][TEXT]        = $model->field["EMERGENCYCALL"];
#        $data["EMERGENCYTELNO"][TEXT]       = $model->field["EMERGENCYTELNO"];
/*コメントおわり */

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = "WHERE SCHREGNO  = '".$model->schregno."'";
        $where .= "  AND ISSUEDATE = date('".str_replace("/", "-", $model->issuedate)."')";

        $query = Query::updateSQL($data,"SCHREG_ADDRESS_DAT",$where);
        $db->query($query);

        //保護者データを作成
        knja110_2Query::update_Guardian_dat($db, $model);

        //急用連絡先を更新(2005/10/20 追加)
        knja110_2Query::updateEmergencyInfo($db, $model, "update");


        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }
    //保護者データを作成
    function update_Guardian_dat(&$db, $model){
        //保護者データがなければ作成する
        $query  = "";
        $query .= "  INSERT INTO guardian_dat (";
        $query .= "      schregno";
        $query .= "     ,relationship";
        $query .= "     ,registercd";
        $query .= "     ,updated";
        $query .= "  )";
        $query .= "   (SELECT ";
        $query .= "           t1.schregno";
        $query .= "          ,'".$model->field["RELATIONSHIP"]."'";
        $query .= "          ,'".STAFFCD."'";
        $query .= "          ,SYSDATE()";
        $query .= "      FROM schreg_base_mst t1";
        $query .= "     WHERE t1.schregno = '".$model->schregno."' AND ";
        $query .= "           NOT EXISTS ( SELECT 'X' FROM guardian_dat w1";
        $query .= "                        WHERE w1.schregno = t1.schregno";
        $query .= "                      )";
        $query .= "   )";

        $db->query($query);

        $data = array();
        $data["RELATIONSHIP"][TEXT]     = $model->field["RELATIONSHIP"];
        $data["GUARD_NAME"][TEXT]       = $model->field["GUARD_NAME"];
        $data["GUARD_KANA"][TEXT]       = $model->field["GUARD_KANA"];
        $data["GUARD_SEX"][TEXT]        = $model->field["GUARD_SEX"];
        $data["GUARD_BIRTHDAY"][TEXT]   = $model->field["GUARD_BIRTHDAY"];
        $data["GUARD_ZIPCD"][TEXT]      = $model->field["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $model->field["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $model->field["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]      = $model->field["GUARD_TELNO"];
        $data["GUARD_FAXNO"][TEXT]      = $model->field["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]     = $model->field["GUARD_E_MAIL"];
        $data["GUARD_JOBCD"][TEXT]      = $model->field["GUARD_JOBCD"];
        $data["GUARD_WORK_NAME"][TEXT]  = $model->field["GUARD_WORK_NAME"];
        $data["GUARD_WORK_TELNO"][TEXT] = $model->field["GUARD_WORK_TELNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  ="WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data,"GUARDIAN_DAT",$where);
        $db->query($query);
    }
    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $query  ="DELETE FROM schreg_address_dat WHERE schregno = '".$model->schregno."' AND issuedate = date('".str_replace("/", "-", $model->issuedate)."')";
        $db->query($query);

        //急用連絡先を削除(2005/10/20 追加)
//        knja110_2Query::updateEmergencyInfo($db, $model, "delete");

        Query::dbCheckIn($db);
        return;
    }


### 2005/10/20 緊急連絡先1,2追加

    //生徒の緊急連絡先情報を取得するSQLを発行
    function getEmergencyInfo($schregno)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     EMERGENCYCALL,  EMERGENCYNAME,  EMERGENCYRELA_NAME,  EMERGENCYTELNO,    ";
        $query .= "     EMERGENCYCALL2, EMERGENCYNAME2, EMERGENCYRELA_NAME2, EMERGENCYTELNO2    ";
        $query .= " FROM SCHREG_BASE_MST ";
        $query .= " WHERE SCHREGNO  = '".$schregno."' ";

        return $query;
    }

    //生徒の緊急連絡先情報を更新
    function updateEmergencyInfo(&$db, &$model, $flg)
    {
        $data = array();

        $data["EMERGENCYCALL"][TEXT]       = ($flg == "update")? $model->field["EMERGENCYCALL"] : "";
        $data["EMERGENCYNAME"][TEXT]       = ($flg == "update")? $model->field["EMERGENCYNAME"] : "";
        $data["EMERGENCYRELA_NAME"][TEXT]  = ($flg == "update")? $model->field["EMERGENCYRELA_NAME"] : "";
        $data["EMERGENCYTELNO"][TEXT]      = ($flg == "update")? $model->field["EMERGENCYTELNO"] : "";

        $data["EMERGENCYCALL2"][TEXT]      = ($flg == "update")? $model->field["EMERGENCYCALL2"] : "";
        $data["EMERGENCYNAME2"][TEXT]      = ($flg == "update")? $model->field["EMERGENCYNAME2"] : "";
        $data["EMERGENCYRELA_NAME2"][TEXT] = ($flg == "update")? $model->field["EMERGENCYRELA_NAME2"] : "";
        $data["EMERGENCYTELNO2"][TEXT]     = ($flg == "update")? $model->field["EMERGENCYTELNO2"] : "";

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  ="WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data,"SCHREG_BASE_MST",$where);
        $db->query($query);
    }

### 2005/10/20 おわり

}
?>
