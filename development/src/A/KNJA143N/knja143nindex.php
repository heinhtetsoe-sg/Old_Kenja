<?php

require_once('for_php7.php');

require_once('knja143nModel.inc');
require_once('knja143nQuery.inc');

class knja143nController extends Controller {
    var $ModelClassName = "knja143nModel";
    var $ProgramID      = "KNJA143N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja143n":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja143nModel();      //コントロールマスタの呼び出し
                    $this->callView("knja143nForm1");
                    exit;
                case "csv":
                    $sessionInstance->downloadCsvFile();
                    $sessionInstance->setCmd("");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja143nCtl = new knja143nController;
//var_dump($_REQUEST);
?>
