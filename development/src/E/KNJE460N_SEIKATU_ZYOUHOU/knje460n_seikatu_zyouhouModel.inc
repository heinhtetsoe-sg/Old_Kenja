<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');
class knje460n_seikatu_zyouhouModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $exp_year;      //左フレームから送られてくる年度
    var $exp_semester;  //左フレームから送られてくる学期
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限
    var $grade;         //左フレームから送られてくる年組
    var $upddate;       //右フレームで設定する更新日

    var $record_date;
    var $hope_moji;          //本人希望の文字数
    var $hope_gyou;          //本人希望の行数
    var $guardian_hope_moji; //保護者希望の文字数
    var $guardian_hope_gyou; //保護者希望の行数

    function knje460n_seikatu_zyouhouModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->upddate);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");

        //左フレームから送られてくる
        //年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        //学籍番号
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        //氏名
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        //年組
        if (VARS::get("GRADE")) {
            $pos = strpos(VARS::get("GRADE"), "-");//ハイフンを含むときは、HR_CLASSはセットしない
            if ($pos === false) {
                $this->grade = VARS::get("GRADE")."-".VARS::get("HR_CLASS");
            } else {
                $this->grade = VARS::get("GRADE");
            }
        }
        //更新日(別画面に引き渡す値。コンボで選択した日付)
        if (VARS::post("UPDDATE")) {
            $this->upddate = VARS::post("UPDDATE");
        }
        //新規を選択した際の日付(入力 or ポップアップで指定した日付)
        if (VARS::post("SELNEWDATE")) {
            $this->selnewdate = VARS::post("SELNEWDATE");
        }
        
        $this->field1 = array("YEAR"             =>  VARS::post("YEAR"),             //更新年度
                              "ENTRANT_NAME"     =>  VARS::post("ENTRANT_NAME"),     //記入者
                              "MY_WISH"          =>  VARS::post("MY_WISH"),          //本人願い
                              "MY_HOPE"          =>  VARS::post("MY_HOPE"),          //本人希望
                              "GUARDIAN_WISH"    =>  VARS::post("GUARDIAN_WISH"),    //保護者願い
                              "GUARDIAN_HOPE"    =>  VARS::post("GUARDIAN_HOPE"),    //保護者希望
                              "GOURITEKI_HAIRYO" =>  VARS::post("GOURITEKI_HAIRYO"), //合理的配慮
                              "ZIRITU"           =>  VARS::post("ZIRITU"),           //目指したい自立の姿
                              "SIEN"             =>  VARS::post("SIEN")              //今年度の支援方針
                             );


        $db = Query::dbCheckOut();

        //RECORD_DATEのMAXを取得
        //$this->record_date = $db->getOne(knje460n_seikatu_zyouhouQuery::getMaxRecordDataQuery($this));
        $this->record_date = $this->exp_year;

        Query::dbCheckIn($db);

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //各項目のサイズ設定
        $this->getFieldSize();

    }

    //各項目のサイズ設定
    function getFieldSize() {
        //本人/保護者の願いと希望の文字数と行数の設定
        $this->hope_moji = 20;
        $this->hope_gyou = 3;
        $this->wish_moji = 20;
        $this->wish_gyou = 3;

        //合理的配慮の文字数と行数の設定
        $this->gouriteki_hairyo_moji = 45;
        $this->gouriteki_hairyo_gyou = 20;
        
        //目指したい自立の姿の文字数と行数の設定
        $this->ziritu_moji = 45;
        $this->ziritu_gyou = 10;
        
        //支援方針の文字数と行数の設定
        $this->sien_moji = 37;
        $this->sien_gyou = 10;

    }
    
    //更新用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();

        //チェック処理
        if ($this->validation()) {
            //更新処理
            if ($this->cmd === 'subform1_update') {
                //SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DATの更新
                knje460n_seikatu_zyouhouQuery::getUpdateMainDat($db, $this);

                //1.現在の生活および将来の生活に関する希望
                knje460n_seikatu_zyouhouQuery::getUpdateMainQuery($db, $this, '01', '01', $this->field1["MY_HOPE"]);
                knje460n_seikatu_zyouhouQuery::getUpdateMainQuery($db, $this, '01', '02', $this->field1["GUARDIAN_HOPE"]);
                knje460n_seikatu_zyouhouQuery::getUpdateMainQuery($db, $this, '01', '03', $this->field1["ENTRANT_NAME"]);
            }
            $this->setMessage("MSG201");
        }

        Query::dbCheckIn($db);

        return;
    }
    function validation() {
        //記入者
        if ($this->field1["ENTRANT_NAME"] == "") {
            $this->setWarning("MSG308");
            return false;
        }

        //本人願い
        if ($this->field1["MY_WISH"] != "") {
            if ($this->validate_row_cnt($this->field1["MY_WISH"], ($this->wish_moji * 2)) > $this->wish_gyou) {
                $this->setWarning("MSG915", "本人願い");
                return false;
            }
        }
        //本人希望
        if ($this->field1["MY_HOPE"] != "") {
            if ($this->validate_row_cnt($this->field1["MY_HOPE"], ($this->hope_moji * 2)) > $this->hope_gyou) {
                $this->setWarning("MSG915", "本人希望");
                return false;
            }
        }
        //保護者願い
        if ($this->field1["GUARDIAN_WISH"] != "") {
            if ($this->validate_row_cnt($this->field1["GUARDIAN_WISH"], ($this->wish_moji * 2)) > $this->wish_gyou) {
                $this->setWarning("MSG915", "保護者願い");
                return false;
            }
        }
        //保護者希望
        if ($this->field1["GUARDIAN_HOPE"] != "") {
            if ($this->validate_row_cnt($this->field1["GUARDIAN_HOPE"], ($this->hope_moji * 2)) > $this->hope_gyou) {
                $this->setWarning("MSG915", "保護者希望");
                return false;
            }
        }
        //合理的配慮
        if ($this->field1["GOURITEKI_HAIRYO"] != "") {
            if ($this->validate_row_cnt($this->field1["GOURITEKI_HAIRYO"], ($this->gouriteki_hairyo_moji * 2)) > $this->gouriteki_hairyo_gyou) {
                $this->setWarning("MSG915", "合理的配慮");
                return false;
            }
        }
        //目指したい自立の姿
        if ($this->field1["ZIRITU"] != "") {
            if ($this->validate_row_cnt($this->field1["ZIRITU"], ($this->ziritu_moji * 2)) > $this->ziritu_gyou) {
                $this->setWarning("MSG915", "目指したい自立の姿");
                return false;
            }
        }
        //今年度の支援方針
        if ($this->field1["SIEN"] != "") {
            if ($this->validate_row_cnt($this->field1["SIEN"], ($this->sien_moji * 2)) > $this->sien_gyou) {
                $this->setWarning("MSG915", "支援方針");
                return false;
            }
        }

        return true;
    }

}
?>
