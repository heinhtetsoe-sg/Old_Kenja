<?php

require_once('for_php7.php');

require_once('knjl324jModel.inc');
require_once('knjl324jQuery.inc');

class knjl324jController extends Controller {
    var $ModelClassName = "knjl324jModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl324j":
                    $sessionInstance->knjl324jModel();
                    $this->callView("knjl324jForm1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjl324jForm1");
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
$knjl324jCtl = new knjl324jController;
//var_dump($_REQUEST);
?>
