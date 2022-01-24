<?php

require_once('for_php7.php');

//定数定義
class knjj120aModel extends Model {
    var $field = array();
    var $cmd;
    var $clubcd = "";
    var $date = "";
    var $seq = "";
    var $div = "";
    var $schregno = "";
    var $clublist = "";
    var $clublist2 = "";

    var $selectdata = "";

    //メンバ変数の初期化
    function knjj120aModel() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->clubcd);
            unset($this->date);
            unset($this->seq);
            unset($this->div);
            unset($this->schregno);
            unset($this->clublist);
            unset($this->schkind);
            unset($this->detail_schkind);
            unset($this->selectdata);
        }

        $this->field = array("CLUBCD" 		    =>  VARS::post("CLUBCD"),
                             "DETAIL_DATE"      =>  VARS::post("DETAIL_DATE"),
                             "MEET_NAME"        =>  VARS::post("MEET_NAME"),
                             "DIV"              =>  VARS::post("DIV"),
                             "SCHREGNO"         =>  VARS::post("SCHREGNO"),
                             "HOSTCD"           =>  VARS::post("HOSTCD"),
                             "ITEMCD"           =>  VARS::post("ITEMCD"),
                             "KINDCD"           =>  VARS::post("KINDCD"),
                             "RECORDCD"         =>  VARS::post("RECORDCD"),
                             "DOCUMENT"         =>  VARS::post("DOCUMENT"),
                             "DETAIL_REMARK"    =>  VARS::post("DETAIL_REMARK"),
                             "DETAIL_SEQ"       =>  VARS::post("DETAIL_SEQ"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->clublist     = VARS::post("CLUBLIST");
        $this->clublist2    = VARS::post("SEND_CLUBLIST");

        $this->detail_schkind = VARS::post("DETAIL_SCHKIND");

        $this->schkind      = VARS::request("SCHKIND");

        if (VARS::get("SEND_CLUBLIST")) {
            $this->clublist2 = (VARS::get("SEND_CLUBLIST") == 'all') ? "" : VARS::get("SEND_CLUBLIST");
        }
        if (VARS::get("CLUBCD")) {
            $this->clubcd = VARS::get("CLUBCD");
        }
        if (VARS::get("DETAIL_SCHKIND")) {
            $this->detail_schkind = VARS::get("DETAIL_SCHKIND");
        }
        if (VARS::get("DETAIL_DATE")) {
            $this->date = VARS::get("DETAIL_DATE");
        }
        if (VARS::get("DETAIL_SEQ")) {
            $this->seq = VARS::get("DETAIL_SEQ");
        }
        if (VARS::get("DIV")) {
            $this->div = VARS::get("DIV");
        }
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }

        $this->selectdata = explode(',',VARS::post("selectdata"));

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //DB接続
        $db = Query::dbCheckOut();

        if ($this->cmd == "update" || $this->cmd == "delete") {
            //リストより選択していない
            if (!$this->clubcd || !$this->date || !$this->seq) {
                $this->setWarning("MSG308");
                return false;
            }

            //キー変更
            if (($this->clubcd  != $this->field["CLUBCD"]) || 
                ($this->date    != $this->field["DETAIL_DATE"]) || 
                ($this->seq     != $this->field["DETAIL_SEQ"]) || 
                ($this->div     != $this->field["DIV"])) {
                $this->setWarning("MSG308");
               return false;
            }

            //個人
            if ($this->field["DIV"] == "1") {
                if (!$this->field["SCHREGNO"]) {
                    $this->setWarning("生徒を指定してください。");
                    return false;
                }
                //リストより選択していない
                if (!$this->schregno) {
                    $this->setWarning("MSG308");
                    return false;
                }
                //キー変更
                if ($this->schregno != $this->field["SCHREGNO"]) {
                    $this->setWarning("MSG308");
                    return false;
                }
            }
        }

        if ($this->cmd == "add") {
            //必須項目チェック
            if (!$this->field["CLUBCD"]) {
                $this->setWarning("MSG301","　　（ 部クラブ ）");
                return false;
            }
            if (!$this->field["DETAIL_DATE"]) {
                $this->setWarning("MSG301","　　（ 日付 ）");
                return false;
            }
            if ($this->field["DIV"] == "1" && !$this->field["SCHREGNO"]) {
                $this->setWarning("MSG301","　　（ 生徒 ）");
                return false;
            }
            if ($this->field["DIV"] == "2" && !implode(',',$this->selectdata)) {
                $this->setWarning("MSG304","　　（ 生徒 ）");
                return false;
            }

            //日付チェック
            $sdate = CTRL_YEAR.'/04/01';
            $edate = (CTRL_YEAR+1).'/03/31';
            if ($this->field["DETAIL_DATE"] < $sdate || $edate < $this->field["DETAIL_DATE"]) {
                $this->setWarning("MSG916","(".$sdate."～".$edate.")");
                return false;
            }

        }

        if ($this->cmd == "update" || $this->cmd == "add") {
            //存在チェック
            $query = knjj120aQuery::checkClubExist($this);
            $cnt = $db->getOne($query);
            if ($this->field["DIV"] == "1") {
                if ($cnt == 0) {
                    $this->setWarning("MSG916","日付が在籍期間外です。");
                    return false;
                }
            } else {
                if (get_count($this->selectdata) != $cnt) {
                    //在籍期間外の生徒取得
                    $query = knjj120aQuery::checkClubExist($this, "sch");
                    $sch = $db->getOne($query);

                    $this->setWarning("MSG916","日付が在籍期間外の生徒が含まれています。\\n( ".$sch." )");
                    return false;
                }
            }
        }

        //文字数チェック
        $moji_check = array("MEET_NAME"       => array("大会名称",    "30"),
                            "DOCUMENT"        => array("記録",        "20"),
                            "DETAIL_REMARK"   => array("備考",        "20"));

        foreach ($moji_check as $key => $val) {
            list($name, $moji) = $val;
            if ($this->validate_row_cnt($this->field[$key], ($moji * 2)) > 1) {
                $this->setWarning("MSG915","({$name}は全角で{$moji}文字までです)");
                return false;
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    //編集完了用のモデルを返します
    function &getInsertModel() {
        if ($this->validateInput()) {
            if ($this->field["DIV"] == "1") {
                //個人
                $this->insertKojin();
            } else {
                //団体
                $this->insertDantai();
            }

            if ($this->cmd == 'add') {
                $this->clublist2 = $this->field["CLUBCD"];
            }
        }
        return;
    }

    //INSERT・UPDATE時の内部メソッド（個人）
    function &insertKojin() {
        $seq = knjj120aQuery::getInsertQueryKojin($this);

        if (!isset($this->warning)) {
            $this->schregno = $this->field["SCHREGNO"];
            $this->clubcd = $this->field["CLUBCD"];
            $this->date = $this->field["DETAIL_DATE"];
            $this->seq = $seq;
            $this->div = 1;

            if ($this->cmd == 'add' && $this->clublist2 && $this->field["CLUBCD"] != $this->clublist2) {
                unset($this->clublist2);
            }

            $this->setMessage("MSG201");
        }
        return;
    }

    //INSERT・UPDATE時の内部メソッド（団体）
    function &insertDantai() {
        $groupcd = knjj120aQuery::getInsertQueryDantai($this);

        if (!isset($this->warning)) {
            $this->clubcd = $this->field["CLUBCD"];
            $this->date = $this->field["DETAIL_DATE"];
            $this->seq = $groupcd;
            $this->div = 2;

            if ($this->cmd == 'add' && $this->clublist2 && $this->field["CLUBCD"] != $this->clublist2) {
                unset($this->clublist2);
            }

            $this->setMessage("MSG201");
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    function &delete() {
        if ($this->field["DIV"] == '1') {
            knjj120aQuery::getDeleteQueryKojin($this);
        } else {
            $db = Query::dbCheckOut();
            knjj120aQuery::getDeleteQueryDantai($db, $this);
            Query::dbCheckIn($db);
        }

        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            $this->clubcd = $this->date = $this->seq = "";
            $this->field = "";
            $this->selectdata = "";
        }
        return;
    }
}
?>
