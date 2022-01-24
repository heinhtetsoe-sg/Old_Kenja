<?php

require_once('for_php7.php');

require_once('knjd192cModel.inc');
require_once('knjd192cQuery.inc');

class knjd192cController extends Controller {
    var $ModelClassName = "knjd192cModel";
    var $ProgramID      = "KNJD192C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_grade":
                case "knjd192c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd192cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192cForm1");
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
$knjd192cCtl = new knjd192cController;
//var_dump($_REQUEST);
?>
