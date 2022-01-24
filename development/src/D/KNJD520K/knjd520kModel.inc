<?php

require_once('for_php7.php');

class knjd520kModel extends Model
{
    var $cmd;
    var $sub_cmb;           //科目
    var $gk_cmb;            //学年
    var $cntl_dt_year;      //処理年度(CTRL_YEAR)
    var $cntl_semester;     //処理学期(CTRL_SEMESTER)
    var $flg;
    var $num;
    var $range_cd = array();
    var $schregno_cnt = array();
    var $field = array();

    function knjd520kModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            $this->num = 0;
            $this->gk_cmb = "";
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cntl_dt_year  = CTRL_YEAR;
        $this->cntl_semester = CTRL_SEMESTER;
        $this->cmd           = VARS::request("cmd");
        $this->sub_cmb       = VARS::post("sub_cmb");

        //学年コンボの値が変わったら科目の値を初期化
        if ($this->gk_cmb != VARS::post("gk_cmb")) {
            $this->sub_cmb = "";
        }

        if (VARS::post("gk_cmb")) {
            $this->gk_cmb = VARS::post("gk_cmb");
            $this->num = 0;
        }

        if (VARS::get("FLG")) {
            $this->flg = VARS::get("FLG");
            $this->sub_cmb = VARS::get("sub_cmb");
        } else {
            $this->flg = 0;
        }

        if (VARS::post("num")) {
            $this->num = VARS::post("num");
        }

        if (VARS::get("gk_cmb")) {
            $this->gk_cmb = VARS::get("gk_cmb");
        }

        if (VARS::get("sub_cmb") == "0") {
            $this->sub_cmb = VARS::get("sub_cmb");
        }

        $this->field = array();
        for($i=0; $i<get_count($this->schregno_cnt); $i++) {
            $this->field["SCHREGNO"][] = $this->schregno_cnt[$i];
            $this->field["EDIT_TEXT"][] = VARS::post("edit_text".$i);
        }

        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = AUTHORITY;
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }


    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //更新処理
        knjd520kQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //成績反映用のモデルを返します
    function &getReflectModel()
    {
        //更新処理
        knjd520kQuery::getReflectModel($this);
        $this->setMessage("MSG201");
    }


    //変数をクリアします
    function clean()
    {
    }
}
?>
