<?php

require_once('for_php7.php');

require_once('knjl324hModel.inc');
require_once('knjl324hQuery.inc');

class knjl324hController extends Controller {
    var $ModelClassName = "knjl324hModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl324h":
                    $sessionInstance->knjl324hModel();
                    $this->callView("knjl324hForm1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjl324hForm1");
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
$knjl324hCtl = new knjl324hController;
//var_dump($_REQUEST);
?>
