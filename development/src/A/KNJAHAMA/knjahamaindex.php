<?php

require_once('for_php7.php');

require_once('knjahamaModel.inc');
require_once('knjahamaQuery.inc');

class knjahamaController extends Controller {
    var $ModelClassName = "knjahamaModel";
    var $ProgramID      = "KNJAHAMA";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjahama":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjahamaModel();        //コントロールマスタの呼び出し
                    $this->callView("knjahamaForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjahamaForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjahamaCtl = new knjahamaController;
//var_dump($_REQUEST);
?>
