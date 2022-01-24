<?php

require_once('for_php7.php');

//定数定義

class knjz351vModel extends Model {
    var $field = array();
    var $cmd;
    var $year;  //年度
    var $selectdata;
    var $schoolKindArray = array();

    //メンバ変数の初期化
    function knjz351vModel()
    {
    }
    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
        }
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");
        if (VARS::get("sendSubclassCd")) {
            $this->getSubclasscd = VARS::get("sendSubclassCd");//パラメータセット用
        }
        $this->field["SAKI_TESTCD"] = VARS::request("SAKI_TESTCD");

        $this->year             = CTRL_YEAR;                        //現在処理年度を初期値にする
        $this->selectdata       = VARS::post("selectdata");
        $this->cmd              = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if (VARS::get("SEND_schoolKind")) {
            $this->sendSchoolKind = VARS::get("SEND_schoolKind");
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
        //学校種別(H:高校 J:中学 P:小学)
        $db = Query::dbCheckOut();
        $this->schoolKindArray = array();
        $query = knjz351vQuery::getNameMstA023();

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                if ($row["NAME1"] != $this->sendSchoolKind) {
                    continue;
                }
            } else if ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                if ($row["NAME1"] != SCHOOLKIND) {
                    continue;
                }
            }
            $this->schoolKindArray[$row["NAME1"]] = "00-" . $row["NAME1"] . "-00-000000";
        }
        $result->free();
        Query::dbCheckIn($db);
    }
    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->year)){
            $this->warning = "対象年度を選択してください。";
            return false;
        }
        if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            $cnt_pre = $db->getOne(knjz351vQuery::chkCopyData($this->year-1, "check"));
            $cnt_this = $db->getOne(knjz351vQuery::chkCopyData($this->year, "check"));
            Query::dbCheckIn($db);

            //存在チェック（前年度）
            if ($cnt_pre == 0) {
                $this->setWarning("MSG917","前年度のデータが存在しません。");
                return false;
            }

            //存在チェック（今年度）
            if ($cnt_this > 0) {
                $this->setWarning("MSG917","今年度にデータが存在します。");
                return false;
            }
        }
        
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        //パーツ
        if ($this->cmd == 'update') {
            $result = knjz351vQuery::getUpdateQuery($this);
        }
        return $result;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            //前年度のデータ取得
            $result = $db->query(knjz351vQuery::chkCopyData($this->year-1, "list"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjz351vQuery::getCopyQuery($this, $row));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }    
}
?>
