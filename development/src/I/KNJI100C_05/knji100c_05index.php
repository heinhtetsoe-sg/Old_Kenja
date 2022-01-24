<?php

require_once('for_php7.php');

require_once('knji100c_05Model.inc');
require_once('knji100c_05Query.inc');
//区分に関しての出力設定
define("OUT_CODE_NAME", 1);
define("OUT_CODE_ONLY", 2);
define("OUT_NAME_ONLY", 3);

class knji100c_05Controller extends Controller {
    var $ModelClassName = "knji100c_05Model";
    var $ProgramID      = "KNJI100C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "changeGrade":
                case "changeSyubetu":
                    $this->callView("knji100c_05Form1");
                    break 2;
                case "csv":
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
$knji100c_05Ctl = new knji100c_05Controller;
?>
