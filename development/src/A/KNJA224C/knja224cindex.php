<?php

require_once('for_php7.php');

require_once('knja224cModel.inc');
require_once('knja224cQuery.inc');

class knja224cController extends Controller {
    var $ModelClassName = "knja224cModel";
    var $ProgramID      = "KNJA224C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja224c":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja224cModel();        //コントロールマスタの呼び出し
                    $this->callView("knja224cForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja224cForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja224cCtl = new knja224cController;
//var_dump($_REQUEST);
?>
