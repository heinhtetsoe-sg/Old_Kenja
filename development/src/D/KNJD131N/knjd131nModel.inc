<?php

require_once('for_php7.php');
class knjd131nModel extends Model {
    var $cmd;
    var $schregno;      //学籍番号
    var $field;         //画面上のデータ
    var $grade;         //学年
    var $control;
    var $exp_year;      //左フレームから送られてくる年度
    var $exp_semester;  //左フレームから送られてくる学期

    //1学期中間
    var $sem1_01_remark_moji;
    var $sem1_01_remark_gyou;
    //1学期期末
    var $sem1_02_remark_moji;
    var $sem1_02_remark_gyou;
    //2学期中間
    var $sem2_01_remark_moji;
    var $sem2_01_remark_gyou;
    //2学期期末
    var $sem2_02_remark_moji;
    var $sem2_02_remark_gyou;
    //3学期期末
    var $sem3_02_remark_moji;
    var $sem3_02_remark_gyou;

    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    
    function knjd131nModel() {
        $this->year = CTRL_YEAR;
        $this->mode = "ungrd";      //在籍モード
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        
        $this->cmd     = VARS::request("cmd");
        //次の生徒
        $this->nextURL = VARS::post("nextURL");

        //左フレームから送られてくる年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //左フレームから送られてくる学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }

        //表示データ
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO"); //学籍番号
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");         //生徒氏名
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");       //学年
        }

        $this->field = array("SEM1_01_REMARK"    => VARS::post("SEM1_01_REMARK"),      //1学期中間
                             "SEM1_02_REMARK"    => VARS::post("SEM1_02_REMARK"),      //1学期期末
                             "SEM2_01_REMARK"    => VARS::post("SEM2_01_REMARK"),      //2学期中間
                             "SEM2_02_REMARK"    => VARS::post("SEM2_02_REMARK"),      //2学期期末
                             "SEM3_02_REMARK"    => VARS::post("SEM3_02_REMARK")       //3学期期末
                             );

        //生徒一覧を更新
        if (VARS::get("init")) {
            unset($this->schregno); //学籍番号
            unset($this->name);     //生徒氏名
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->getFieldSize();
    }

    //入力項目チェック
    function validateInput() {
        if (!isset($this->schregno)) return false;
        //1学期中間
        if ($this->validate_row_cnt($this->field["SEM1_01_REMARK"], ($this->sem1_01_remark_moji * 2)) > $this->sem1_01_remark_gyou) {
            $this->setWarning("MSG915","(".$this->SemesterName1."中間考査の特記事項は{$this->sem1_01_remark_gyou}行までです)");
            return false;
        }
        //1学期期末
        if ($this->validate_row_cnt($this->field["SEM1_02_REMARK"], ($this->sem1_02_remark_moji * 2)) > $this->sem1_02_remark_gyou) {
            $this->setWarning("MSG915","(".$this->SemesterName1."期末考査の特記事項は{$this->sem1_02_remark_gyou}行までです)");
            return false;
        }
        //2学期中間
        if ($this->validate_row_cnt($this->field["SEM2_01_REMARK"], ($this->sem2_01_remark_moji * 2)) > $this->sem2_01_remark_gyou) {
            $this->setWarning("MSG915","(".$this->SemesterName2."中間考査の特記事項は{$this->sem2_01_remark_gyou}行までです)");
            return false;
        }
        //2学期期末
        if ($this->validate_row_cnt($this->field["SEM2_02_REMARK"], ($this->sem2_02_remark_moji * 2)) > $this->sem2_02_remark_gyou) {
            $this->setWarning("MSG915","(".$this->SemesterName2."期末考査の特記事項は{$this->sem2_02_remark_gyou}行までです)");
            return false;
        }
        //3学期期末
        if ($this->validate_row_cnt($this->field["SEM3_02_REMARK"], ($this->sem3_02_remark_moji * 2)) > $this->sem3_02_remark_gyou) {
            $this->setWarning("MSG915","(".$this->SemesterName3."期末考査は{$this->sem3_02_remark_gyou}行までです)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            if ($this->cmd == "update") {
                knjd131nQuery::update($db, $this);
                //knjd131nQuery::update2($db, $this);
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    function getFieldSize() {
        //1学期中間
        $this->sem1_01_remark_moji = 25; //デフォルトの値
        $this->sem1_01_remark_gyou = 8; //デフォルトの値
        //1学期期末
        $this->sem1_02_remark_moji = 25; //デフォルトの値
        $this->sem1_02_remark_gyou = 8; //デフォルトの値
        //2学期中間
        $this->sem2_01_remark_moji = 25; //デフォルトの値
        $this->sem2_01_remark_gyou = 8; //デフォルトの値
        //2学期期末
        $this->sem2_02_remark_moji = 25; //デフォルトの値
        $this->sem2_02_remark_gyou = 8; //デフォルトの値
        //3学期期末
        $this->sem3_02_remark_moji = 25; //デフォルトの値
        $this->sem3_02_remark_gyou = 8; //デフォルトの値
    }
}
?>
