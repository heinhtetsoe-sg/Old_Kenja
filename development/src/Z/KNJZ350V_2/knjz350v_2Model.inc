<?php

require_once('for_php7.php');

//定数定義

class knjz350v_2Model extends Model {
    var $field = array();
    var $cmd;
    var $year;  //年度
    var $schoolKindArray = array();

    //メンバ変数の初期化
    function knjz350v_2Model()
    {
    }
    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
        }
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");
        if (VARS::get("sendSubclassCd")) {
            $this->field["SUBCLASSCD"] = VARS::get("sendSubclassCd");
        }
        $this->field["NOT_AUTO_KANSAN_FLG"] = VARS::post("NOT_AUTO_KANSAN_FLG");
        $this->field["KANSAN_AFTER_UPDATE_FLG"] = VARS::post("KANSAN_AFTER_UPDATE_FLG");
        $this->field["SAKI_TESTCD"] = '9990009';

        $this->year             = CTRL_YEAR;                        //現在処理年度を初期値にする
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
        $query = knjz350v_2Query::getNameMstA023();
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
            $result = knjz350v_2Query::getUpdateQuery($this);
        }
        return $result;
    }
}
?>
