<?php

require_once('for_php7.php');

require_once('knjd621bModel.inc');
require_once('knjd621bQuery.inc');

class knjd621bController extends Controller {
    var $ModelClassName = "knjd621bModel";
    var $ProgramID      = "KNJD621B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd621b":
                    $sessionInstance->knjd621bModel();
                    $this->callView("knjd621bForm1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjd621bForm1");
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
$knjd621bCtl = new knjd621bController;
?>
