<?php

require_once('for_php7.php');
class knjxattend_htrainremarkModel extends Model {
    var $cmd;
    var $schregno;          //学籍番号
    var $year;              //年度
    var $output_field;      //出力フィールド名

    function knjxattend_htrainremarkModel() {}

    function init() {
        $this->schregno         = VARS::request("SCHREGNO");
        $this->year             = VARS::request("YEAR");
        $this->output_field     = VARS::request("OUTPUT_FIELD") ? VARS::request("OUTPUT_FIELD") : "ATTENDREC_REMARK";
        $this->output_height    = VARS::request("OUTPUT_HEIGHT") ? VARS::request("OUTPUT_HEIGHT") : "75";
        $this->output_width     = VARS::request("OUTPUT_WIDTH") ? VARS::request("OUTPUT_WIDTH") : "200";

        $this->getPropertiesAll();

        if ($this->Properties["useTitleShukkou"] == "1") {
            $this->attendTitle = "出校";
        } else {
            $this->attendTitle = "出欠";
        }
    }
}
?>
