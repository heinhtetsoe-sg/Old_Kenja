<?php

require_once('for_php7.php');

require_once('knji100c_14Model.inc');
require_once('knji100c_14Query.inc');
//区分に関しての出力設定
define("OUT_CODE_NAME", 0);
define("OUT_CODE_ONLY", 1);
define("OUT_NAME_ONLY", 2);

class knji100c_14Controller extends Controller {
    var $ModelClassName = "knji100c_14Model";
    var $ProgramID      = "KNJI100C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "changeGrade":
                case "edit":
                    $this->callView("knji100c_14Form1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getCsvModel()) {
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit");
                        break 1;
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knji100c_14Ctl = new knji100c_14Controller;
?>
