<?php

require_once('for_php7.php');

require_once('knjl325hModel.inc');
require_once('knjl325hQuery.inc');

class knjl325hController extends Controller {
    var $ModelClassName = "knjl325hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl325h":
                    $sessionInstance->knjl325hModel();
                    $this->callView("knjl325hForm1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjl325hForm1");
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
$knjl325hCtl = new knjl325hController;
//var_dump($_REQUEST);
?>
