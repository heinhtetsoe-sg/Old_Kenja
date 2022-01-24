<?php

require_once('for_php7.php');

require_once('knjd621cModel.inc');
require_once('knjd621cQuery.inc');

class knjd621cController extends Controller {
    var $ModelClassName = "knjd621cModel";
    var $ProgramID      = "KNJD621C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd621c":
                    $sessionInstance->knjd621cModel();
                    $this->callView("knjd621cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd621cForm1");
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
$knjd621cCtl = new knjd621cController;
?>
