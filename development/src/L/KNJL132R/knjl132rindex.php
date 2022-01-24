<?php

require_once('for_php7.php');

require_once('knjl132rModel.inc');
require_once('knjl132rQuery.inc');

class knjl132rController extends Controller {
    var $ModelClassName = "knjl132rModel";
    var $ProgramID      = "KNJL132R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl132r":                                //��˥塼���̤⤷����SUBMIT�������
                    $sessionInstance->knjl132rModel();        //����ȥ�����ޥ����θƤӽФ�
                    $this->callView("knjl132rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("̤�б��Υ��������{$sessionInstance->cmd}�Ǥ�"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl132rCtl = new knjl132rController;
//var_dump($_REQUEST);
?>
