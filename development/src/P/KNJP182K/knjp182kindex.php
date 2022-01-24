<?php

require_once('for_php7.php');

require_once('knjp182kModel.inc');
require_once('knjp182kQuery.inc');

class knjp182kController extends Controller {
    var $ModelClassName = "knjp182kModel";
    var $ProgramID      = "KNJP182K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv": // CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp182kForm1");
                    }
                    break 2;
                case "":
                case "knjp182k": // メニュー画面もしくはSUBMITした場合
                case "change_class": // クラス変更時のSUBMITした場合
                    $sessionInstance->knjp182kModel();
                    $this->callView("knjp182kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp182kCtl = new knjp182kController;
//var_dump($_REQUEST);
?>
