<?php

require_once('for_php7.php');

require_once('knjd236cModel.inc');
require_once('knjd236cQuery.inc');

class knjd236cController extends Controller {
    var $ModelClassName = "knjd236cModel";
    var $ProgramID      = "KNJD236C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd236c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd236cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd236cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd236cForm1");
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
$knjd236cCtl = new knjd236cController;
//var_dump($_REQUEST);
?>
