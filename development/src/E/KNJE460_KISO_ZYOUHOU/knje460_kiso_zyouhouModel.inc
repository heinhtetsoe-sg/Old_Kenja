<?php

require_once('for_php7.php');

class knje460_kiso_zyouhouModel extends Model {
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

    var $kiso_zyouhou_moji;  //基礎情報の文字数
    var $kiso_zyouhou_gyou;  //基礎情報の行数
    var $target = "";

    function knje460_kiso_zyouhouModel() {
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
            unset($this->target);
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

        $this->field2 = array("YEAR"            =>  VARS::post("YEAR"),         //更新年度
                              "PASTYEAR"        =>  VARS::post("PASTYEAR"),     //過年度
                              "PASTYEARLOADFLG" =>  VARS::post("HID_PASTYEARLOADFLG"), //過年度読込みフラグ
                              "ENTRANT_NAME"    =>  VARS::post("ENTRANT_NAME"), //記入者
                              "SELECT_COUNT"    =>  VARS::post("SELECT_COUNT"), //項目数
                              "KISO_ZYOUHOU"    =>  VARS::post("KISO_ZYOUHOU")  //基礎情報
                             );
        $this->target       = VARS::get("TARGET");

        //基礎情報選択の格納
        for ($idx = 1;$idx <= $this->field2["SELECT_COUNT"];$idx++) {
            $this->field2["SPRT_SEQ".$idx]   = VARS::post("SPRT_SEQ".$idx);
            $this->field2["BASE_TITLE".$idx] = VARS::post("BASE_TITLE".$idx);
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
        //基礎情報の文字数と行数の設定
        $this->kiso_zyouhou_moji = 50;
        $this->kiso_zyouhou_gyou = 40;
    }
    
    //更新用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();

        //更新処理
        if ($this->cmd === 'subform1_update') {
            //SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DATの更新
            knje460_kiso_zyouhouQuery::getUpdateMainDat($db, $this);

            //2.支援をする上での基礎情報
            knje460_kiso_zyouhouQuery::getUpdateMainQuery($db, $this, '02', '01', $this->field2["KISO_ZYOUHOU"]);
        }

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }


}
?>
