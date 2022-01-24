<?php

require_once('for_php7.php');

class knje460_zirituModel extends Model {
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

    var $ziritu_moji;  //自立の文字数
    var $ziritu_gyou;  //自立の行数

    function knje460_zirituModel() {
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

        $this->field4 = array("YEAR"            =>  VARS::post("YEAR"),         //更新年度
                              "PASTYEAR"        =>  VARS::post("PASTYEAR"),     //過年度
                              "PASTYEARLOADFLG" =>  VARS::post("HID_PASTYEARLOADFLG"), //過年度読込みフラグ
                              "ENTRANT_NAME"    =>  VARS::post("ENTRANT_NAME"), //記入者
                              "SELECT_COUNT"    =>  VARS::post("SELECT_COUNT")  //項目数
                             );

        //基礎情報選択の格納
        for ($idx = 1;$idx <= $this->field4["SELECT_COUNT"];$idx++) {
            $this->field4["SPRT_SEQ".$idx]   = VARS::post("SPRT_SEQ".$idx);
            $this->field4["GOAL_TITLE".$idx] = VARS::post("GOAL_TITLE".$idx);
            $this->field4["ZIRITU".$idx]     = VARS::post("ZIRITU".$idx);
        }

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
        //自立の文字数と行数の設定
        $this->ziritu_moji = 42;
        $this->ziritu_gyou = 3;
    }

    //更新用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();

        //更新処理
        if ($this->cmd === 'subform1_update') {
            //SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DATの更新
            knje460_zirituQuery::getUpdateMainDat($db, $this);

            //DELETE
            knje460_zirituQuery::getDeleteMainQuery($db, $this, '04');
            for ($idx = 1;$idx <= $this->field4["SELECT_COUNT"];$idx++) {
                //INSERT
                knje460_zirituQuery::getUpdateMainQuery($db, $this, '04', $this->field4["SPRT_SEQ".$idx], $this->field4["ZIRITU".$idx]);
            }
        }

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }


}
?>
