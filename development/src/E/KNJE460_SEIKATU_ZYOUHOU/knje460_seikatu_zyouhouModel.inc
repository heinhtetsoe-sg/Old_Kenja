<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');
class knje460_seikatu_zyouhouModel extends Model {
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

    function knje460_seikatu_zyouhouModel() {
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
        
        $this->field1 = array("YEAR"            =>  VARS::post("YEAR"),         //更新年度
                              "PASTYEAR"        =>  VARS::post("PASTYEAR"),     //過年度
                              "PASTYEARLOADFLG" =>  VARS::post("HID_PASTYEARLOADFLG"), //過年度読込みフラグ
                              "ENTRANT_NAME"    =>  VARS::post("ENTRANT_NAME"), //記入者
                              "HOPE"            =>  VARS::post("HOPE"),         //本人希望
                              "GUARDIAN_HOPE"   =>  VARS::post("GUARDIAN_HOPE") //保護者希望
                             );


        $db = Query::dbCheckOut();

        //RECORD_DATEのMAXを取得
        $this->record_date = $db->getOne(knje460_seikatu_zyouhouQuery::getMaxRecordDataQuery($this));

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
        //本人希望の文字数と行数の設定
        $this->hope_moji = 23;
        $this->hope_gyou = 4;

        //保護者希望の文字数と行数の設定
        $this->guardian_hope_moji = 23;
        $this->guardian_hope_gyou = 4;

    }
    
    //更新用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();

        //更新処理
        if ($this->cmd === 'subform1_update') {
            //SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DATの更新
            knje460_seikatu_zyouhouQuery::getUpdateMainDat($db, $this);

            //1.現在の生活および将来の生活に関する希望
            knje460_seikatu_zyouhouQuery::getUpdateMainQuery($db, $this, '01', '01', $this->field1["HOPE"]);
            knje460_seikatu_zyouhouQuery::getUpdateMainQuery($db, $this, '01', '02', $this->field1["GUARDIAN_HOPE"]);
            knje460_seikatu_zyouhouQuery::getUpdateMainQuery($db, $this, '01', '03', $this->field1["ENTRANT_NAME"]);
        }

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }


}
?>
