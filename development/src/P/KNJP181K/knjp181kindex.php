<?php

require_once('for_php7.php');

require_once('knjp181kModel.inc');
require_once('knjp181kQuery.inc');

class knjp181kController extends Controller {
    var $ModelClassName = "knjp181kModel";
    var $ProgramID      = "KNJP181K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv": // CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp181kForm1");
                    }
                    break 2;
                case "":
                case "knjp181k": // メニュー画面もしくはSUBMITした場合
                case "change_class": // クラス変更時のSUBMITした場合
                    $sessionInstance->knjp181kModel();
                    $this->callView("knjp181kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp181kCtl = new knjp181kController;
//var_dump($_REQUEST);
?>
