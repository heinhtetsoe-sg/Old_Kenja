<?php

require_once('for_php7.php');

/*定数定義*/
class knjz252Model extends Model {

    var $cmd;                        //cmd
    var $field      = array();       //エレメント値
    var $arrTitle   = array();       //データ保持用
    var $arrName    = array();       //データ保持用

    /*メンバ変数の初期化*/
    function knjz252Model() {

    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            $this->prgId = "";
            $this->field = array();
            unset($this->schoolKind);
        }
        if (VARS::get("SEND_PROGRAMID")) {
            $this->prgId = VARS::get("SEND_PROGRAMID");
        }
        $this->maxSeq = 10;

        $this->field["R_PROGRAMID"] = VARS::post("R_PROGRAMID");
        for ($seqCnt = 1; $seqCnt <= $this->maxSeq; $seqCnt++) {
            $this->field["R_TITLE".$seqCnt]     = VARS::post("R_TITLE".$seqCnt);
            $this->field["R_FILE_NAME".$seqCnt] = VARS::post("R_FILE_NAME".$seqCnt);
            $this->arrTitle[$seqCnt] = (!strlen($this->field["R_TITLE".$seqCnt])) ? "" : $this->field["R_TITLE".$seqCnt];
            $this->arrName[$seqCnt]  = (!strlen($this->field["R_FILE_NAME".$seqCnt])) ? "" : $this->field["R_FILE_NAME".$seqCnt];
        }
        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput($type) {
        //更新、削除時にリストから選択しているかをチェック
        if ($type == "update" || $type == "delete") {
            if($this->prgId == "" || $this->prgId != $this->field["R_PROGRAMID"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        if ($type == "update" || $type == "insert") {
            //必須項目
            if (trim($this->field["R_PROGRAMID"]) == "") {
                $this->setWarning("MSG310","プログラムID");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["R_PROGRAMID"]) > 20 ) {
                $this->setWarning("MSG915", "( プログラムID ) 20バイトまで");
                return false;
            }
            foreach ($this->arrTitle as $seqCnt => $val ) {
                if (strlen($this->field["R_FILE_NAME".$seqCnt]) > 90 ) {
                    $this->setWarning("MSG915", "( 印影欄表示名称{$seqCnt}番目 ) 90バイトまで");
                    return false;
                }
            }
        }
        if ($type == "insert") {
            //レコード取得
            $db = Query::dbCheckOut();
            $query      = knjz252Query::countPrgStampDat($this);
            $result     = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $checkPrgID = $row["PROGRAMID"];
            }
            Query::dbCheckIn($db);
            //重複チェック
            if ($this->field["R_PROGRAMID"] == $checkPrgID) {
                $this->setWarning("MSG302", "プログラムID");
                return false;
            }
        }
        return true;
    }

    /*編集完了用のモデルを返します*/
    function &getUpdateModel() {
        //入力チェック
          if ($this->validateInput("update")) {
            if ($this->update()) {
                $this->setMessage("MSG201");
                return;
            }
        }
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            if($this->insert()){
                $this->setMessage("MSG201");
                $this->prgId = $this->field["R_PROGRAMID"];
            }
        }
    }

    /*削除用のモデルを返します*/
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            if($this->delete()){
                $this->setMessage("MSG201");
                unset($this->field);
            }
        }
    }

    function &delete() {
        $result = knjz252Query::getDeleteQuery($this, "");
        return $result;
    }

    function &insert() {
        $result = knjz252Query::getInsertQuery($this);
        return $result;
    }

    function &update() {
        $result = knjz252Query::getUpdateQuery($this);
        return $result;
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        //存在チェック用に件数取得
        $db = Query::dbCheckOut();
        $this_year  = CTRL_YEAR;
        $pre_year   = CTRL_YEAR - 1;
        $this_year_cnt = $db->getOne(knjz252Query::chkPrgStampDat($this, $this_year));
        $pre_year_cnt = $db->getOne(knjz252Query::chkPrgStampDat($this, $pre_year));
        Query::dbCheckIn($db);

        //今年度のデータチェック
        if ($this_year_cnt > 0) {
            $this->setWarning("MSG203", "対象年度にデータが存在します。");
            return;
        }
        //前年度のデータチェック
        if ($pre_year_cnt == 0) {
            $this->setWarning("MSG203", "前年度にデータが存在しません。");
            return;
        }

        //コピー処理
        knjz252Query::getCopyQuery($this);
        $this->setMessage("MSG201");
    }

    /*変数をクリアします*/
    function clean() {
    }
}
?>
