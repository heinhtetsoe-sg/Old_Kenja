<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp733Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $schoolKind;

    /**
     * メンバ変数の初期化
    */
    function knjp733Model() {
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
//            $this->vSchregRegdDat = knjp733Query::getViewSchregRegdDat();
        }
        $this->cmd    = VARS::request("cmd");
        //年次
        $this->annual = VARS::post("ANNUAL");
        //組
        $this->hr_class = VARS::post("HR_CLASS");
        //コース
        $this->coursecode = VARS::post("COURSECODE");
        $this->schregno   = VARS::post("SCHREGNO");

        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        $this->monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");

        //画面上のデータ
        $this->field = array("GRADE"                => VARS::post("GRADE"),             //学年
                             "CMC"                  => VARS::post("CMC"),               //課程学科コース
                             "OFFSET_REFUND_DIV"    => VARS::post("OFFSET_REFUND_DIV"), //ラジオ(1:相殺, 2:還付)
                             "REDUCTION_KIND"       => VARS::post("REDUCTION_KIND"),    //種別
                             "PROCESS_DATE"         => VARS::post("PROCESS_DATE"),      //処理日
                             "MONTH_FROM"           => VARS::post("MONTH_FROM"),        //期間（FROM）
                             "MONTH_TO"             => VARS::post("MONTH_TO")           //期間（TO）
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function &getMainModel() {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel() {

        foreach ($this->gradeArray as $grade => $gLabel) {
            if ($this->field["GRADE"] != '00' && $this->field["GRADE"] != $grade) {
                continue;
            }
            foreach ($this->cmcArray as $cmc => $cmcLabel) {
                if ($this->field["CMC"] != '0:000:0000' && $this->field["CMC"] != $cmc) {
                    continue;
                }
                //実行
                knjp733Query::executeReduction($this, $grade, $cmc);
            }
        }

        //実行履歴
        knjp733Query::executeRireki($this);

        $this->setMessage("MSG201");
        return;
    }
}
?>
