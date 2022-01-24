<?php

require_once('for_php7.php');


class knjd210wModel extends Model
{
    var $grade;
    var $school_kind;
    var $exam;
    var $seme;
    var $kind;
    var $item;
    var $sdiv;
    var $seme_sch;
    var $cmd;
    var $control;
    var $chairdate;
    var $subclasscd;
    var $electdiv;
    var $tableStr;
    var $schoolName;
    var $assess_chk;
//    var $calcDeviationFlg;

    function knjd210wModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->course_major = VARS::post("COURSE_MAJOR"); //課程学科コンボ
        $this->grade    = VARS::post("GRADE");
        $this->school_kind = VARS::post("SCHOOL_KIND"); //学校種別
        $this->exam     = VARS::post("EXAM");
        $test = explode("-", VARS::post("EXAM"));
        $this->seme     = VARS::post("SEMESTER");
        $this->kind     = $test[0];
        $this->item     = $test[1];
        $this->sdiv     = $test[2];
        $this->seme_sch = ($this->seme != "9") ? $this->seme : CTRL_SEMESTER;
        $this->chairdate = VARS::post("CHAIRDATE");
        $this->subclasscd = VARS::post("SUBCLASSCD");
        $this->assess_chk = VARS::post("ASSESS_CHK");
        //メニュー起動時
        if (VARS::get("PROGRAMID")) {
            $this->electdiv = "1"; //初期値
        } else {
            $this->electdiv = VARS::post("ELECTDIV");
        }
        //更新テーブル _dat:record_rank_dat _v_dat:record_rank_v_dat _sdiv_dat:record_rank_sdiv_dat
        $this->tableStr = "_sdiv_dat";
//echo "更新テーブル：record_rank" .$this->tableStr;
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //学校名取得
        $db = Query::dbCheckOut();
        $query = knjd210wQuery::getNameMstZ010();
        $this->schoolName = $db->getOne($query);
        //合算科目の母集団から除くフラグ 1:合併元除く 2:合併先除く
        $dx70 = $db->getOne(knjd210wQuery::getNameMstD070($this, "D{$this->school_kind}70"));
        $d070 = $db->getOne(knjd210wQuery::getNameMstD070($this, "D070"));
        $this->d070Namespare1 = strlen($dx70) ? $dx70 : $d070;
        Query::dbCheckIn($db);
    }

    function &getMainModel()
    {
    }

    //事前処理チェック
    function validateInput()
    {
        //存在チェック（在籍データ）
        $cnt_sch = knjd210wQuery::ChecktoSchregno($this);
        if (0 < $cnt_sch) {
        } else {
            $this->setWarning("MSG303", "\\n(学籍在籍データ)\\n学年または組またはコースが設定されているか確認して下さい。");
            return false;
        }
        //存在チェック（成績データ）
        $cnt_rec = knjd210wQuery::ChecktoRecordScore($this);
        if (0 < $cnt_rec) {
        } else {
            $this->setWarning("MSG303", "\\n(成績データ)");
            return false;
        }

        return true;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //事前処理チェック
        if ($this->validateInput()) {
            ////満点で偏差値計算を判別（1人でも科目の満点が複数ある場合、合算科目の偏差値は、各科目の偏差値の平均を算出する）
            //$rtnCnt = knjd210wQuery::getPerfectCnt($this);
            //$this->calcDeviationFlg = (0 < $rtnCnt) ? "1" : "";
            knjd210wQuery::executeRireki($this);
            if (knjd210wQuery::ExecuteQuery($this)) {
                $this->setMessage("MSG202");
            }
        }
    }

    // デバッグ
    function debugTime($text, $query)
    {
        //echo "<div style=\"color:white;\">".date("G:i:s")." ".$text."<div>".$query."</div></div>";
    }
}
?>
