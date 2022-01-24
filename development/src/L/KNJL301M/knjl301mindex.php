<?php

require_once('for_php7.php');

require_once('knjl301mModel.inc');
require_once('knjl301mQuery.inc');

class knjl301mController extends Controller {
    var $ModelClassName = "knjl301mModel";
    var $ProgramID      = "KNJL301M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301m":
                    $sessionInstance->knjl301mModel();
                    $this->callView("knjl301mForm1");
                    exit;
				case "csv":         //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjl301mForm1");
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
$knjl301mCtl = new knjl301mController;
?>
