<?php

require_once('for_php7.php');

require_once('knjl308eModel.inc');
require_once('knjl308eQuery.inc');

class knjl308eController extends Controller {
    var $ModelClassName = "knjl308eModel";
    var $ProgramID      = "KNJL308E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl308e":
                    $this->callView("knjl308eForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl308eCtl = new knjl308eController;
?>
