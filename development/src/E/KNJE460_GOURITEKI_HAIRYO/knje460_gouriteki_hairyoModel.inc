<?php

require_once('for_php7.php');

class knje460_gouriteki_hairyoModel extends Model {
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

    var $gouriteki_hairyo_moji;  //合理的配慮の文字数
    var $gouriteki_hairyo_gyou;  //合理的配慮の行数

    function knje460_gouriteki_hairyoModel() {
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

        $this->field3 = array("YEAR"                =>  VARS::post("YEAR"),             //更新年度
                              "PASTYEAR"            =>  VARS::post("PASTYEAR"),         //過年度
                              "PASTYEARLOADFLG"     =>  VARS::post("HID_PASTYEARLOADFLG"), //過年度読込みフラグ
                              "ENTRANT_NAME"        =>  VARS::post("ENTRANT_NAME"),     //記入者
                              "GOURITEKI_HAIRYO"    =>  VARS::post("GOURITEKI_HAIRYO")  //合理的配慮
                             );

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
        //合理的配慮の文字数と行数の設定
        $this->gouriteki_hairyo_moji = 50;
        $this->gouriteki_hairyo_gyou = 20;
    }
    
    //更新用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();

        //更新処理
        if ($this->cmd === 'subform1_update') {
            //SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DATの更新
            knje460_gouriteki_hairyoQuery::getUpdateMainDat($db, $this);

            //3.合理的配慮
            knje460_gouriteki_hairyoQuery::getUpdateMainQuery($db, $this, '03', '01', $this->field3["GOURITEKI_HAIRYO"]);
        }

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }


}
?>
