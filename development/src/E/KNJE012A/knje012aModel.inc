<?php

require_once('for_php7.php');
class knje012aModel extends Model {
    var $cmd;
    var $schregno;      //学籍番号
    var $field;         //画面上のデータ
    var $grade;         //学年
    var $annual;        //学年(年次)
    var $control;
    var $exp_year;      //左フレームから送られてくる年度
    var $exp_semester;  //左フレームから送られてくる学期

    //総合的な学習の時間の記録
    var $totalstudyval_moji;
    var $totalstudyval_gyou;
    //特別活動の記録
    var $specialactrec_moji;
    var $specialactrec_gyou;
    //行動の記録
    var $behaverec_remark_moji;
    var $behaverec_remark_gyou;
    //総合所見及び指導上参考となる諸事項
    var $trin_ref_moji;
    var $trin_ref_gyou;
    //健康の記録
    var $healthrec_moji;
    var $healthrec_gyou;
    //出欠の記録
    var $attendrec_remark_moji;
    var $attendrec_remark_gyou;

    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    
    function knje012aModel() {
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

        //表示科目
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO"); //学籍番号
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");         //生徒氏名
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");       //学年
        }

        $this->field = array("TOTALSTUDYVAL"    => VARS::post("TOTALSTUDYVAL"),      //総合的な学習の時間の記録
                             "SPECIALACTREC"    => VARS::post("SPECIALACTREC"),      //特別活動の記録
                             "BEHAVEREC_REMARK" => VARS::post("BEHAVEREC_REMARK"),   //行動の記録
                             "HEALTHREC"        => VARS::post("HEALTHREC"),          //健康の記録
                             "TRIN_REF"         => VARS::post("TRIN_REF"),           //総合所見及び指導上参考となる諸事項
                             "ATTENDREC_REMARK" => VARS::post("ATTENDREC_REMARK")    //出欠の記録
                             );

        //学年（年次）
        if (VARS::post("ANNUAL")) {
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

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
        //総合的な学習の時間の記録
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ($this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
            $this->setWarning("MSG915","(総合的な学習の時間の記録は{$this->totalstudyval_gyou}行までです)");
            return false;
        }
        //特別活動の記録
        if ($this->validate_row_cnt($this->field["SPECIALACTREC"], ($this->specialactrec_moji * 2)) > $this->specialactrec_gyou) {
            $this->setWarning("MSG915","(特別活動の記録は{$this->specialactrec_gyou}行までです)");
            return false;
        }
        //行動の記録
        if ($this->validate_row_cnt($this->field["BEHAVEREC_REMARK"], ($this->behaverec_remark_moji * 2)) > $this->behaverec_remark_gyou) {
            $this->setWarning("MSG915","(行動の記録は{$this->behaverec_remark_gyou}行までです)");
            return false;
        }
        //総合所見及び指導上参考となる諸事項
        if ($this->validate_row_cnt($this->field["TRIN_REF"], ($this->trin_ref_moji * 2)) > $this->trin_ref_gyou) {
            $this->setWarning("MSG915","(総合所見及び指導上参考となる諸事項は{$this->trin_ref_gyou}行までです)");
            return false;
        }
        //健康の記録
        if ($this->validate_row_cnt($this->field["HEALTHREC"], ($this->healthrec_moji * 2)) > $this->healthrec_gyou) {
            $this->setWarning("MSG915","(健康の記録は{$this->healthrec_gyou}行までです)");
            return false;
        }
        //出欠の記録
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915","(出欠の記録は{$this->attendrec_remark_gyou}行までです)");
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
                knje012aQuery::update($db, $this);
                knje012aQuery::update2($db, $this);
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    function getFieldSize() {
        //総合的な学習の時間の記録
        $this->totalstudyval_moji = 25; //デフォルトの値
        $this->totalstudyval_gyou = 11; //デフォルトの値
        //特別活動の記録
        $this->specialactrec_moji = 25; //デフォルトの値
        $this->specialactrec_gyou = 11; //デフォルトの値
        //行動の記録
        $this->behaverec_remark_moji = 25; //デフォルトの値
        $this->behaverec_remark_gyou = 8; //デフォルトの値
        //総合所見及び指導上参考となる諸事項
        $this->trin_ref_moji = 25; //デフォルトの値
        $this->trin_ref_gyou = 8; //デフォルトの値
        //健康の記録
        $this->healthrec_moji = 25; //デフォルトの値
        $this->healthrec_gyou = 8; //デフォルトの値
        //出欠の記録
        $this->attendrec_remark_moji = 6; //デフォルトの値
        $this->attendrec_remark_gyou = 6; //デフォルトの値
    }
}
?>
