<?php

require_once('for_php7.php');

require_once('knjj210Model.inc');
require_once('knjj210Query.inc');

class knjj210Controller extends Controller {
    var $ModelClassName = "knjj210Model";
    var $ProgramID      = "KNJJ210";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj210":                                //��˥塼���̤⤷����SUBMIT�������
                    $sessionInstance->knjj210Model();        //����ȥ�����ޥ����θƤӽФ�
                    $this->callView("knjj210Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("̤�б��Υ��������{$sessionInstance->cmd}�Ǥ�"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjj210Ctl = new knjj210Controller;
var_dump($_REQUEST);
?>
