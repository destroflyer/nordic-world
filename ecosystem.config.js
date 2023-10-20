module.exports = {
  apps: [
    {
      name: 'nordic-world',
      script: '/usr/lib/jvm/java-17-openjdk-amd64/bin/java',
      args: '-jar nordic-world.jar',
      exp_backoff_restart_delay: 100,
    },
  ],
};
