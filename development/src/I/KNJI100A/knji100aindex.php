<?php

require_once('for_php7.php');

require_once('knji100aModel.inc');
require_once('knji100aQuery.inc');
//区分に関しての出力設定
define("OUT_CODE_NAME", 0);
define("OUT_CODE_ONLY", 1);
define("OUT_NAME_ONLY", 2);

class knji100aController extends Controller {
    var $ModelClassName = "knji100aModel";
    var $ProgramID      = "KNJI100A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knji100aForm1");
                    break 2;
                case "csv":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getCsvModel()){
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
$knji100aCtl = new knji100aController;
?>
