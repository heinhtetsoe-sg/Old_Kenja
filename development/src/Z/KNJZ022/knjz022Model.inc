<?php

require_once('for_php7.php');
class knjz022Model extends Model {
    var $cmd;
    var $control;
    var $field = array();
    var $year;

    function knjz022Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        //年度
        if (VARS::get("year") != "") {
            $this->year = VARS::get("year");
        }

        $this->field = array("FINSCHOOLCD"          => VARS::post("FINSCHOOLCD"),
                             "SCHOOL_TYPE"          => VARS::post("SCHOOL_TYPE"),
                             "COURSE_CD"            => VARS::post("COURSE_CD"),
                             "ATTENDANCE_FLAG"      => VARS::post("ATTENDANCE_FLAG"),
                             "KENJA_URL"            => VARS::post("KENJA_URL")
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
            if (VARS::request("SENDSCHKIND") != "") {
                $this->sendSchkind = VARS::request("SENDSCHKIND"); //校種受け取り
            }
        }

        //教育委員会、学校サーバ情報
        $db = Query::dbCheckOut();
        $query = knjz022Query::getDataBaseInfo();
        $this->dataBaseinfo = $db->getOne($query);
        Query::dbCheckIn($db);
    }

    //入力チェック
    function validateInput() {
        //学校コード存在チェック
        if ($this->dataBaseinfo != '2') {
            $db = Query::dbCheckOut();
            $finschoolcd = $db->getOne(knjz022Query::getFinschoolMst($this->field["FINSCHOOLCD"]));
            Query::dbCheckIn($db);
            if ($this->field["FINSCHOOLCD"] != "" && $finschoolcd == "") {
                $this->setWarning("MSG901", "( 学校コード )");
                return false;
            }
        }

        //桁数チェック
        if ($this->validate_row_cnt($this->field["KENJA_URL"], 90) > 1) {
            $this->setWarning("MSG915","(URLは90バイトまでです)");
            return false;
        }

        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            //更新処理
            knjz022Query::updateSchoolDetailDat($this);
            $this->setMessage("MSG201");
            return true;
        }
        return false;
    }
}
?>
